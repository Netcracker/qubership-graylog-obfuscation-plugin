import "../css/ObfuscationPlugin.css";
import React from "react";
import { ObjectsTable } from "./ObjectsTable";

export class OneFieldObjectsTable extends React.Component {
  constructor(props) {
    super(props);
    this.elements = [];
  }

  renderColumn() {
    return (key, value, ref) => {
      let checkResult = this.props.checkValue(value);
      return (
        <>
          {!checkResult.valid && (
            <div className="error">{checkResult.message}</div>
          )}
          <input
            ref={ref}
            list={this.props.tableName}
            className={this.getClassName(checkResult)}
            type="text"
            name={this.props.columnName}
            value={value}
          />
        </>
      );
    };
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
        this.toValueArray({ after: (strings) => strings.push("") }),
      );
  }

  setInputValues() {
    return (inputValues) => (this.elements = inputValues);
  }

  getClassName(checkResult) {
    let className = "form-control";
    if (!checkResult.valid) {
      className += " error-input";
    }

    return className;
  }

  toValueArray({ filter, after } = {}) {
    let strings = [];

    for (let element of this.elements) {
      if (!filter || (filter && filter(element))) {
        strings.push(element.item.value);
      }
    }

    after && after(strings);

    return strings;
  }

  getValues() {
    return this.props.values.map((element) => ({ item: element }));
  }

  getAvailableValues() {
    return this.props.avialableValues.filter(
      (value) => !this.props.values.includes(value),
    );
  }

  render() {
    return (
      <>
        <ObjectsTable
          tableName={this.props.tableName}
          columnMapping={{ item: this.props.columnName }}
          values={this.getValues()}
          setInputValues={this.setInputValues()}
          renderColumn={this.renderColumn()}
          onChange={this.onChange()}
          onRemoveElement={this.onRemoveElement()}
          onAddElement={this.onAddElement()}
          help={this.props.help}
        />
        <datalist id={this.props.tableName}>
          {this.getAvailableValues().map((value) => (
            <option value={value} />
          ))}
        </datalist>
      </>
    );
  }
}
