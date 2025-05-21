import { useState } from "react";
import { API_BASE } from "../config";

export default function User({ messages, threadId, assistantId, auth }) {
  const [inputValue, setInputValue] = useState("");

  const handleInputChange = (event) => {
    setInputValue(event.target.value);
  };

  const handleSend = async () => {
    if (!inputValue) return;

    try {
      fetch(`${API_BASE}/api/prompt/thread`, {
        method: "POST",
        headers: {
          Authorization: auth,
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          assistantId,
          threadId,
          prompt: inputValue,
        }),
      });

      setInputValue("");
    } catch (error) {
      console.error("Error sending message:", error);
    }
  };

  return (
    <div className="user-panel">
      <h2>User Interaction</h2>
      {messages &&
        messages.map((message, i) => (
          <div class="message user-message" key={i}>
            {message.content}
          </div>
        ))}
      <div className="input-box">
        <input
          type="text"
          placeholder="Type a message..."
          value={inputValue}
          onChange={handleInputChange}
        />
        <button onClick={handleSend}>Send</button>
      </div>
    </div>
  );
}
