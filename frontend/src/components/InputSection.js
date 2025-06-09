function InputSection({inputValue, onInputChange, onSend, onKeyDown,}) {
    return (
        <div className="input-section">
            <div className="triangle-wrapper">
                <div className="triangle" onClick={onSend}></div>
            </div>
            <textarea
                type="text"
                placeholder="Type a message..."
                value={inputValue}
                onChange={onInputChange}
                onKeyDown={onKeyDown}
            ></textarea>
        </div>
    );
}

export default InputSection;
