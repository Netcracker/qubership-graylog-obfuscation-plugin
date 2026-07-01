import '../css/ObfuscationPlugin.css'
import React from 'react';
import {HelpBlock} from 'react-bootstrap';
import IconButton from 'components/common/IconButton';

export class ObjectsTable extends React.Component {

    constructor(props) {
        super(props);
    }

    _onRemove(index) {
        return () => {
            this.props.onRemoveElement(index);
        }
    }

    renderColumns(element, object) {
        return Object.keys(this.props.columnMapping).map(key => {
            let ref = ref => object[key] = ref;
            return <td>{this.props.renderColumn(key, element[key], ref)}</td>;
        });
    }

    renderRow(element, object, index) {
        return (
            <tr>
                {this.renderColumns(element, object)}
                <td className="change-cell">
                    <IconButton name="delete" title="Remove row" onClick={this._onRemove(index)}/>
                </td>
            </tr>
        );
    }

    renderHeaders() {
        let values = Object.values(this.props.columnMapping);
        let headers = [];
        let lastIndex = values.length - 1;

        for (let i = 0; i < lastIndex; i++) {
            headers.push(<th scope="col">{values[i]}</th>);
        }

        headers.push(<th scope="col" colSpan="2">{values[lastIndex]}</th>);

        return headers;
    }

    renderRows() {
        let index = 0;
        let inputValues = [];
        this.props.setInputValues(inputValues);

        return this.props.values.map(element => {
            let object = {'index': index};
            inputValues.push(object);

            return this.renderRow(element, object, index++);
        });
    }

    getColumnsCount() {
        return Object.entries(this.props.columnMapping).length;
    }

    render() {
        return (
            <table className="table table-striped table-bordered table-condensed" onChange={this.props.onChange}>
                <caption>
                    <p>{this.props.tableName}</p>
                    <p>{this.props.help && <HelpBlock>{this.props.help}</HelpBlock>}</p>
                </caption>
                <thead>
                <tr>
                    {this.renderHeaders()}
                </tr>
                </thead>
                <tfoot>
                <tr>
                    <td colSpan={this.getColumnsCount()}/>
                    <td className="change-cell">
                        <IconButton name="add" title="Add row" onClick={this.props.onAddElement}/>
                    </td>
                </tr>
                </tfoot>
                <tbody>{this.renderRows()}</tbody>
            </table>
        );
    }
}
