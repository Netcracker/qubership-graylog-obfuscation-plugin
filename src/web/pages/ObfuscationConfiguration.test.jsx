import { ObfuscationConfiguration } from "./ObfuscationConfiguration";

function componentWithState(state) {
  const component = new ObfuscationConfiguration({});
  component.state = {
    ...component.state,
    ...state,
  };
  return component;
}

describe("ObfuscationConfiguration validation", () => {
  test("rejects duplicate field names", () => {
    const component = componentWithState({
      "field-names": ["message", "message"],
    });

    expect(component.checkFieldNameValue()("message")).toEqual({
      valid: false,
      message: "Duplicate field name",
    });
    expect(component.isFieldNamesValid()).toBe(false);
  });

  test("rejects stream titles that are not available in Graylog", () => {
    const component = componentWithState({
      streams: ["Audit logs"],
      "stream-titles": ["Missing stream"],
    });

    expect(component.checkStreamTitleValue()("Missing stream")).toEqual({
      valid: false,
      message: "Stream is not found",
    });
    expect(component.isStreamTitlesValid()).toBe(false);
  });

  test("rejects regular expressions reported as invalid by compile test endpoint", () => {
    const component = componentWithState({
      regexCompileStatus: {
        "[": {
          description: "Unclosed character class",
          index: 0,
        },
      },
    });

    expect(component.checkPattern("[")).toEqual({
      valid: false,
      message: "Unclosed character class. Position: 0",
    });
  });

  test("requires pipeline and message filter processors before obfuscation processor", () => {
    const component = componentWithState({
      messageProcessors: {
        disabled_processors: [],
        processor_order: [
          { class_name: "obfuscation", name: "Message Obfuscator" },
          { class_name: "pipeline", name: "Pipeline Processor" },
          { class_name: "message-filter-chain", name: "Message Filter Chain" },
        ],
      },
    });

    expect(component.checkMessageProcessors()).toEqual({
      valid: false,
      message: expect.stringContaining(
        "Pipeline Processor message processor should be higher in order",
      ),
    });
  });
});
