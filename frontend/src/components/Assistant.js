export default function Assistant({ messages }) {
  return (
    <div className="assistant-panel">
      <h2>Assistant Messages</h2>
      {messages &&
        messages.map((message, i) => (
          <div class="message assistant-message" key={i}>
            {message.content}
          </div>
        ))}
    </div>
  );
}
