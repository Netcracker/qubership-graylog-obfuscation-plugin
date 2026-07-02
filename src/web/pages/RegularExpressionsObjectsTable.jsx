import "../css/ObfuscationPlugin.css";
import React from "react";
import { ObjectsTable } from "./ObjectsTable";

export class RegularExpressionsObjectsTable extends React.Component {
  constructor(props) {
    super(props);
    this.elements = [];
  }

  renderColumn() {
    return (key, value, ref) => {
      let checkResult = this.props.checkValue(key, value, this.props.values);
      return (
        <>
          {!checkResult.valid && (
            <div className="error">{checkResult.message}</div>
          )}
          {this.getInput(key, value, ref, checkResult)}
        </>
      );
    };
  }

  getInput(key, value, ref, checkResult) {
    let className = this.getClassName(key, checkResult);
    switch (key) {
      case "id":
        return (
          <input
            ref={ref}
            className={className}
            type="number"
            name={key}
            value={value}
            min="0"
            max="2147483647"
          />
        );
      case "importance":
        return (
          <input
            ref={ref}
            className={className}
            type="number"
            name={key}
            value={value}
            min="-2147483648"
            max="2147483647"
          />
        );
      case "name":
      case "pattern":
        return (
          <input
            ref={ref}
            className={className}
            type="text"
            name={key}
            value={value}
          />
        );
    }
  }

  setInputValues() {
    return (inputValues) => (this.elements = inputValues);
  }

  onChange() {
    return () => this.props.onChange(this.toValueArray());
  }

  onRemoveElement() {
    return (index) =>
      this.props.onChange(
        this.toValueArray({ filter: (element) => element.index !== index }),
      );
  }

  onAddElement() {
    return () =>
      this.props.onChange(
        this.toValueArray({
          after: (values) =>
            values.push({
              id: this.getNextId(values),
              name: "",
              pattern: "",
              importance: 1,
            }),
        }),
      );
  }

  getNextId(values) {
    if (values.length === 0) {
      return 1;
    } else {
      return Math.max(...values.map((object) => object.id)) + 1;
    }
  }

  toValueArray({ filter, after } = {}) {
    let values = [];

    for (let element of this.elements) {
      if (!filter || (filter && filter(element))) {
        values.push({
          id: parseInt(element.id.value),
          name: element.name.value,
          pattern: element.pattern.value,
          importance: this.parseImportance(element),
        });
      }
    }

    after && after(values);

    return values;
  }

  parseImportance(element) {
    if (element.hasOwnProperty("importance")) {
      return parseInt(element.importance.value);
    } else {
      return -1;
    }
  }

  getClassName(key, checkResult) {
    let className = "";
    if (!checkResult.valid) {
      className = "error-input ";
    }

    switch (key) {
      case "id":
      case "importance":
        return className + "number-input form-control";
      case "name":
      case "pattern":
        return className + "form-control";
    }
  }

  render() {
    return (
      <ObjectsTable
        tableName={this.props.tableName}
        columnMapping={this.props.elementMapping}
        values={this.props.values}
        setInputValues={this.setInputValues()}
        renderColumn={this.renderColumn()}
        onChange={this.onChange()}
        onRemoveElement={this.onRemoveElement()}
        onAddElement={this.onAddElement()}
        help={this.props.help}
      />
    );
  }
}
