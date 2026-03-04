import speech_recognition as sr
import sys

def dictate():
    recognizer = sr.Recognizer()
    try:
        with sr.Microphone() as source:
            print("Listening...", flush=True)
            recognizer.adjust_for_ambient_noise(source, duration=0.5)
            audio = recognizer.listen(source, timeout=5, phrase_time_limit=10)
        
        print("Processing...", flush=True)
        text = recognizer.recognize_google(audio)
        print("SUCCESS:" + text, flush=True)
    except sr.WaitTimeoutError:
        print("ERROR:No speech detected within the timeout.", flush=True)
    except sr.UnknownValueError:
        print("ERROR:Could not understand the audio.", flush=True)
    except sr.RequestError as e:
        print(f"ERROR:Could not request results; {e}", flush=True)
    except Exception as e:
        print(f"ERROR:{e}", flush=True)

if __name__ == "__main__":
    dictate()
