from flask import Flask, request, Response, jsonify
from transformers import AutoTokenizer, AutoModelForCausalLM
import torch
import argparse

app = Flask(__name__)
model = None
tokenizer = None

MODEL = "google/gemma-3-1b-it"  # or "meta-llama/Llama-3.2-1B"

def prepareLlamaBot():
    global model, tokenizer
    print(f"Loading {MODEL} model... This may take a while.")

    tokenizer = AutoTokenizer.from_pretrained(MODEL)
    tokenizer.pad_token = tokenizer.eos_token if tokenizer.pad_token is None else tokenizer.pad_token

    model = AutoModelForCausalLM.from_pretrained(
        MODEL,
        # device_map="auto",
        # torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32
    )
    print("Model and tokenizer loaded successfully.")

@app.route('/')
def index():
    return "Welcome to the Llama Chatbot API!"

@app.route('/chat', methods=['POST'])
def chat():
    global model, tokenizer

    user_message = request.form.get('userMessage') or request.get_data(as_text=True).strip()
    if not user_message:
        return Response("Error: userMessage cannot be empty", status=400, mimetype='text/plain')

    print("\nReceived Request:")
    print(f"userMessage: {user_message}")

    prompt = f"""Answer the following question concisely and in one sentence only.\n\nQuestion: {user_message}\nAnswer:"""

    try:
        inputs = tokenizer(prompt, return_tensors="pt", truncation=True, max_length=512, padding=True)
        if torch.cuda.is_available():
            inputs = {k: v.cuda() for k, v in inputs.items()}

        with torch.no_grad():
            outputs = model.generate(
                input_ids=inputs['input_ids'],
                attention_mask=inputs['attention_mask'],
                max_new_tokens=100,
                min_new_tokens=1,
                do_sample=True,
                top_p=0.85,
                temperature=0.6,
                pad_token_id=tokenizer.pad_token_id,
                no_repeat_ngram_size=2
            )

        raw_output = tokenizer.decode(outputs[0], skip_special_tokens=True).strip()
        if raw_output.startswith(prompt):
            raw_output = raw_output[len(prompt):].strip()

        print(f"Raw Model Output: {raw_output}")

        # Clean and validate output
        response = raw_output.strip()
        response = response.replace("```", "").replace("**", "").replace("Answer:", "").strip()

        # Remove backticks or lone symbols
        response = response.strip("`").strip('"').strip()

        # Pick the most meaningful line (last non-empty one)
        lines = [line.strip() for line in response.splitlines() if line.strip()]
        response = lines[-1] if lines else ""

        # Fallback only if fully empty or nonsense
        if not response or response.lower() in ["none", "error", "unknown", "`", ".", ":", ""] or len(response) < 2:
            response = f"Sorry, I couldn't provide a relevant answer to: '{user_message}'. Please rephrase."

        return jsonify({'message': response})
    except Exception as e:
        error_msg = f"Error during generation: {str(e)}"
        print(error_msg)
        return jsonify({'message': error_msg})

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, default=5000, help='Specify the port number')
    args = parser.parse_args()

    port_num = args.port
    prepareLlamaBot()
    print(f"App running on port {port_num}")
    app.run(host='0.0.0.0', port=port_num)
