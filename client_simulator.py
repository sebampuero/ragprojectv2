import uuid
import threading
import requests
import websocket
import sys
import time

user_id = str(uuid.uuid4())
base_url = "http://localhost:8080"
ws_url = "ws://localhost:8080"

promoted_event = threading.Event()

def listen_events():
    url = f"{base_url}/events?userId={user_id}"
    print(f"[*] Connecting to SSE endpoint: {url}")
    try:
        # Long lived connection for SSE
        response = requests.get(url, stream=True)
        response.raise_for_status()
        for line in response.iter_lines():
            if line:
                decoded_line = line.decode('utf-8')
                if decoded_line.startswith("data:"):
                    # The SSE data payload could contain PROMOTED_CHAT or WAIT_IN_Q
                    data = decoded_line[5:].strip()
                    # Strip any potential quotes if serialized as JSON by Spring Boot
                    data = data.replace('"', '')
                    print(f"\n[SSE Event] Received: {data}")
                    
                    if data == "PROMOTED_CHAT":
                        promoted_event.set()
                    elif data == "WAIT_IN_Q":
                        print("[*] Waiting in queue...")
    except Exception as e:
        print(f"\n[SSE Error] {e}")

def start_sse():
    t = threading.Thread(target=listen_events, daemon=True)
    t.start()
    return t

def on_message(ws, message):
    print(f"\n[Backend]: {message}")
    print("You: ", end="", flush=True)

def on_error(ws, error):
    print(f"\n[WebSocket Error]: {error}")

def on_close(ws, close_status_code, close_msg):
    print("\n[*] WebSocket Connection closed")

def on_open(ws):
    print("\n[*] WebSocket Connected!")
    print("You can start typing your messages. Type 'exit' to quit.")
    
    def run(*args):
        while True:
            try:
                user_input = input("You: ")
                if user_input.lower() == 'exit':
                    ws.close()
                    break
                # Only send if not empty
                if user_input.strip():
                    ws.send(user_input)
            except EOFError:
                break
    # Start the input thread so it doesn't block the WebSocket client thread
    threading.Thread(target=run, daemon=True).start()

def main():
    print(f"[*] Generated User ID: {user_id}")
    start_sse()
    
    print("[*] Waiting for PROMOTED_CHAT event to start WebSocket...")
    try:
        # Wait until promoted to chat
        while not promoted_event.is_set():
            time.sleep(0.1)
            
        print("\n[*] Promoted! Initializing WebSocket connection...")
        ws_app = websocket.WebSocketApp(
            f"{ws_url}/websocket?userId={user_id}",
            on_open=on_open,
            on_message=on_message,
            on_error=on_error,
            on_close=on_close
        )
        ws_app.run_forever()
    except KeyboardInterrupt:
        print("\n[*] Exiting simulator...")
        sys.exit(0)

if __name__ == "__main__":
    main()
