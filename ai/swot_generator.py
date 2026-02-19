import sys
import os
import json
import warnings
from io import StringIO
from contextlib import redirect_stdout, redirect_stderr

warnings.filterwarnings("ignore")
os.environ["HF_HUB_DISABLE_PROGRESS_BARS"] = "1"
os.environ["TRANSFORMERS_VERBOSITY"] = "error"
os.environ["TRANSFORMERS_NO_ADVISORY_WARNINGS"] = "1"
os.environ["TOKENIZERS_PARALLELISM"] = "false"
os.environ["PYTHONIOENCODING"] = "utf-8"

try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

MODEL_ID = "TinyLlama/TinyLlama-1.1B-Chat-v1.0"

HF_TOKEN = os.getenv("HF_TOKEN")  

def build_prompt(name: str, description: str, sector: str) -> str:
    return f"""
You are a senior business consultant.
Generate a strong SWOT analysis for the startup below.

Output rules (STRICT):
- Output ONLY valid JSON, nothing else.
- Keys exactly: strengths, weaknesses, opportunities, threats
- Each value: list of 4 to 6 short, specific strings (no generic filler).
- Make points clearly related to the description/sector.

Startup Name: {name}
Sector: {sector}
Description: {description}

JSON:
""".strip()

def extract_first_json_object(text: str):
    start = text.find("{")
    if start == -1:
        return None

    depth = 0
    in_string = False
    escape = False

    for i in range(start, len(text)):
        ch = text[i]
        if in_string:
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == "\"":
                in_string = False
            continue
        else:
            if ch == "\"":
                in_string = True
                continue
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    return text[start:i+1]
    return None

def normalize_swot(obj):
    keys = ["strengths", "weaknesses", "opportunities", "threats"]
    out = {}
    for k in keys:
        v = obj.get(k, [])
        if not isinstance(v, list):
            v = []
        cleaned = []
        for item in v:
            if isinstance(item, str):
                s = item.strip()
                if s:
                    cleaned.append(s)
        out[k] = cleaned[:6]
    return out

def main():
    if len(sys.argv) < 4:
        print(json.dumps({"error": "Usage: swot_generator.py <name> <description> <sector>"}))
        sys.exit(1)

    name = sys.argv[1]
    description = sys.argv[2]
    sector = sys.argv[3]
    prompt = build_prompt(name, description, sector)

    from transformers import AutoTokenizer, AutoModelForCausalLM, TextGenerationPipeline
    from transformers.utils import logging as hf_logging
    hf_logging.set_verbosity_error()
    try:
        hf_logging.disable_progress_bar()
    except Exception:
        pass

    sink = StringIO()
    with redirect_stdout(sink), redirect_stderr(sink):
        tokenizer = AutoTokenizer.from_pretrained(MODEL_ID, token=HF_TOKEN)
        model = AutoModelForCausalLM.from_pretrained(MODEL_ID, token=HF_TOKEN)
        gen = TextGenerationPipeline(model=model, tokenizer=tokenizer)

        result = gen(
            prompt,
            max_new_tokens=260,
            do_sample=True,
            temperature=0.6,
            top_p=0.9,
            repetition_penalty=1.15,
            return_full_text=True
        )[0]["generated_text"]

    json_str = extract_first_json_object(result)
    if not json_str:
        print(json.dumps({"error": "No JSON found"}))
        sys.exit(2)

    try:
        data = json.loads(json_str)
    except Exception:
        print(json.dumps({"error": "JSON parse failed"}))
        sys.exit(3)

    data = normalize_swot(data)
    print(json.dumps(data, ensure_ascii=False))  # ✅ ONLY JSON on stdout
    sys.exit(0)

if __name__ == "__main__":
    main()