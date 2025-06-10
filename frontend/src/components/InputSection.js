function InputSection({inputValue, onInputChange, onSend, onKeyDown, disabled=false}) {
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
                disabled={disabled}
            ></textarea>
        </div>
    );
}

export default InputSection;
