import '../css/ObfuscationPlugin.css'
import React from 'react';
import StoreProvider from 'injection/StoreProvider';
import request from 'superagent-bluebird-promise';

import {Modal} from 'react-bootstrap';
import {Button} from 'components/graylog';
import {BootstrapModalWrapper, Input} from 'components/bootstrap';
import {OneFieldObjectsTable} from './OneFieldObjectsTable';
import {IfPermitted, LoadingIndicator} from 'components/common';
import {RegularExpressionsObjectsTable} from './RegularExpressionsObjectsTable';

export class ObfuscationConfiguration extends React.Component {

    constructor(props) {
        super(props);
        this.modal = null;
        this.prefixURL = '/api/plugins/com.netcracker.graylog2.plugin';
        this.regexes = [];
        this.regexTestCompileRequestId = null;
        this.processorName = 'Message Obfuscator';
        this.requiredProcessorNames = ['Pipeline Processor', 'Message Filter Chain'];
        this.state = {
            'is-obfuscation-enabled': false,
            'sensitive-regular-expressions': [],
            'white-regular-expressions': [],
            'field-names': [],
            'stream-titles': [],
            'text-replacer': '',
            'text-replacers': [],
            isReady: false,
            streams: [],
            fields: [],
            regexCompileStatus: {},
            messageProcessors: {},
            error: null
        };
        this.reloadConfiguration = this.reloadConfiguration.bind(this);
    }

    componentDidMount() {
        this.reloadConfiguration();
    }

    componentDidUpdate() {
        let expressions = this.getWhiteRegularExpressions().map(regex => regex.pattern)
            .concat(this.getSensitiveRegularExpressions().map(regex => regex.pattern));

        if (this.isExpressionsWasUpdated(expressions)) {
            this.regexes = expressions;
            clearTimeout(this.regexTestCompileRequestId);
            this.regexTestCompileRequestId = setTimeout(() =>
                request.post(this.prefixURL + '/obfuscation/regex/compile/test')
                    .send({expressions})
                    .set('X-Requested-With', 'XMLHttpRequest')
                    .set('X-Requested-By', 'XMLHttpRequest')
                    .set('Content-Type', 'application/json')
                    .auth(this.sessionId(), 'session')
                    .then(result => this.updateState({regexCompileStatus: result.body})), 500);
        }
    }

    isExpressionsWasUpdated(newExpressions) {
        if (this.regexes.length === newExpressions.length) {
            for (let i = 0; i < this.regexes.length; i++) {
                if (this.regexes[i] !== newExpressions[i]) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    reloadConfiguration() {
        this.updateState({
            isReady: false,
            error: null,
        });

        Promise.all([
            this.getWithPrefix('/obfuscation/configuration')
                .then(result => this.updateState({...result.body})),
            this.get('/api/streams')
                .then(result => this.updateState({streams: result.body.streams.map(stream => stream.title)})),
            this.get('/api/system/fields')
                .then(result => this.updateState({fields: result.body.fields})),
            this.getWithPrefix('/obfuscation/replacers')
                .then(result => this.updateState({'text-replacers': result.body['text-replacers']})),
            this.get('/api/system/messageprocessors/config')
                .then(result => this.updateState({messageProcessors: result.body})),
        ]).then(() => {
            let updatedState = {isReady: true};
            let checkResult = this.checkMessageProcessors();
            if (!checkResult.valid) {
                updatedState.error = checkResult.message;
            }

            this.updateState(updatedState);
        });
    }

    sessionId() {
        return StoreProvider.getStore('Session').getSessionId();
    }

    getWithPrefix(url) {
        return this.get(this.prefixURL + url);
    }

    get(url) {
        return request.get(url)
            .set('X-Requested-With', 'XMLHttpRequest')
            .set('X-Requested-By', 'XMLHttpRequest')
            .auth(this.sessionId(), 'session');
    }

    installConfiguration() {
        return request.post(this.prefixURL + '/obfuscation/configuration')
            .send(this.getConfiguration())
            .set('X-Requested-With', 'XMLHttpRequest')
            .set('X-Requested-By', 'XMLHttpRequest')
            .set('Content-Type', 'application/json')
            .auth(this.sessionId(), 'session');
    }

    openModal() {
        return () => {
            this.reloadConfiguration();
            this.modal.open();
        }
    }

    closeModal() {
        return () => this.modal.close();
    }

    saveConfig() {
        return () => this.installConfiguration()
            .then(() => this.modal.close())
            .catch(error => this.updateState({error: error.res.text}));
    }

    resetConfig() {
        return () => {
            request.put(this.prefixURL + '/obfuscation/configuration/reset')
                .set('X-Requested-With', 'XMLHttpRequest')
                .set('X-Requested-By', 'XMLHttpRequest')
                .auth(this.sessionId(), 'session')
                .then(this.reloadConfiguration);
        };
    }

    updateState(state) {
        this.setState(previousState => ({
            ...previousState,
            ...state,
        }));
    }

    setObfuscationEnabled() {
        return () => {
            this.updateState({'is-obfuscation-enabled': this.refs['is-obfuscation-enabled'].getChecked()});
        };
    }

    setTextReplacer() {
        return () => this.updateState({'text-replacer': this.refs['text-replacer'].getValue()});
    }

    setFieldNames() {
        return fieldNames => this.updateState({'field-names': fieldNames});
    }

    setStreamTitles() {
        return streamTitles => this.updateState({'stream-titles': streamTitles});
    }

    setSensitiveRegularExpressions() {
        return regularExpressions => this.updateState({'sensitive-regular-expressions': regularExpressions});
    }

    setWhiteRegularExpressions() {
        return regularExpressions => this.updateState({'white-regular-expressions': regularExpressions});
    }

    getSensitiveRegularExpressions() {
        return this.state['sensitive-regular-expressions'];
    }

    getWhiteRegularExpressions() {
        return this.state['white-regular-expressions'];
    }

    getFieldNames() {
        return this.state['field-names'];
    }

    getStreamTitles() {
        return this.state['stream-titles'];
    }

    isObfuscationEnabled() {
        return this.state['is-obfuscation-enabled'];
    }

    getTextReplacer() {
        return this.state['text-replacer'];
    }

    getConfiguration() {
        return {
            'is-obfuscation-enabled': this.isObfuscationEnabled(),
            'sensitive-regular-expressions': this.getSensitiveRegularExpressions(),
            'white-regular-expressions': this.getWhiteRegularExpressions(),
            'field-names': this.getFieldNames(),
            'stream-titles': this.getStreamTitles(),
            'text-replacer': this.getTextReplacer(),
        };
    }

    isReady() {
        return this.state.isReady;
    }

    getRegexCompiledStatus(regex) {
        return this.state.regexCompileStatus[regex];
    }

    getAvailableStreamTitles() {
        return this.state.streams;
    }

    getAvailableFieldNames() {
        return this.state.fields;
    }

    isConfigurationValid() {
        return this.isFieldNamesValid() &&
            this.isStreamTitlesValid() &&
            this.isRegexConfigurationValid(this.getSensitiveRegularExpressions()) &&
            this.isRegexConfigurationValid(this.getWhiteRegularExpressions()) &&
            this.isMessageProcessorValid();
    }

    isMessageProcessorValid() {
        let checkResult = this.checkMessageProcessors();
        return checkResult.valid;
    }

    checkMessageProcessors() {
        let disabledProcessors = this.getDisabledProcessors();
        let processorsOrder = this.getProcessorsOrder();
        let obfuscationProcessorIndex = processorsOrder.indexOf(this.processorName);

        for (let requiredProcessor of this.requiredProcessorNames) {
            if (disabledProcessors.includes(requiredProcessor)) {
                return {
                    valid: false,
                    message: `The ${requiredProcessor}  message processor cannot be disabled.
                        Stream filter will be disabled`
                };
            }

            if (processorsOrder.indexOf(requiredProcessor) > obfuscationProcessorIndex) {
                return {
                    valid: false,
                    message: `The ${requiredProcessor} message processor should be higher in order,
                        than ${this.processorName}. Stream filter will be disabled`
                };
            }
        }

        return {valid: true};
    }

    getDisabledProcessors() {
        let processorClassNameMap = this.state.messageProcessors['processor_order']
            .map(processor => ({[processor.class_name]: processor.name}))
            .reduce(Object.assign);

        return this.state.messageProcessors['disabled_processors'].map(processor => processorClassNameMap[processor]);
    }

    getProcessorsOrder() {
        return this.state.messageProcessors['processor_order'].map(processor => processor.name);
    }

    isFieldNamesValid() {
        let fieldNameValidator = this.checkFieldNameValue();
        for (let fieldName of this.getFieldNames()) {
            if (!fieldNameValidator(fieldName).valid) {
                return false;
            }
        }

        return true;
    }

    isStreamTitlesValid() {
        let streamTitlesValidator = this.checkStreamTitleValue();
        for (let streamTitle of this.getStreamTitles()) {
            if (!streamTitlesValidator(streamTitle).valid) {
                return false;
            }
        }

        return true;
    }

    isRegexConfigurationValid(regexConfiguration) {
        let regexValidator = this.checkRegularExpressionValue();

        for (let whiteRegex of regexConfiguration) {
            for (let [key, value] of Object.entries(whiteRegex)) {
                if (!regexValidator(key, value, regexConfiguration).valid) {
                    return false;
                }
            }
        }

        return true;
    }

    checkFieldNameValue() {
        return fieldName => {
            if (fieldName === '') {
                return {
                    valid: false,
                    message: 'Field name cannot be empty'
                };
            } else if (this.isDuplicateFieldName(fieldName)) {
                return {
                    valid: false,
                    message: 'Duplicate field name'
                };
            } else {
                return {valid: true};
            }
        };
    }

    isDuplicateFieldName(fieldName) {
        return this.getFieldNames().filter(name => name === fieldName).length > 1;
    }

    checkStreamTitleValue() {
        return streamTitle => {
            if (streamTitle === '') {
                return {
                    valid: false,
                    message: 'Stream title cannot be empty'
                };
            } else if (!this.state.streams.includes(streamTitle)) {
                return {
                    valid: false,
                    message: 'Stream is not found'
                };
            } else if (this.isDuplicateStreamTitle(streamTitle)) {
                return {
                    valid: false,
                    message: 'Duplicate stream title'
                };
            } else {
                return {valid: true};
            }
        };
    }

    isDuplicateStreamTitle(streamTitle) {
        return this.getStreamTitles().filter(title => title === streamTitle).length > 1
    }

    checkRegularExpressionValue() {
        return (key, value, values) => {
            switch (key) {
                case 'id':
                    return this.checkId(value, values);
                case 'name':
                    return this.checkName(value);
                case 'pattern':
                    return this.checkPattern(value);
                case 'importance':
                    return this.checkImportance(value);
            }
        };
    }

    checkId(id, values) {
        if (Number.isNaN(id)) {
            return {
                valid: false,
                message: 'ID cannot be empty'
            };
        } else if (this.isDuplicateId(id, values)) {
            return {
                valid: false,
                message: 'Duplicate ID'
            };
        } else {
            return {valid: true};
        }
    }

    isDuplicateId(expectedId, values) {
        return values
            .map(element => element.id)
            .filter(id => id === expectedId).length > 1;
    }

    checkName(name) {
        if (name === '') {
            return {
                valid: false,
                message: 'Name cannot be empty'
            };
        } else {
            return {valid: true};
        }
    }

    checkPattern(pattern) {
        let compilationStatus = this.getRegexCompiledStatus(pattern);
        if (pattern === '') {
            return {
                valid: false,
                message: 'Pattern cannot be empty'
            };
        } else if (compilationStatus) {
            return {
                valid: false,
                message: `${compilationStatus.description}. Position: ${compilationStatus.index}`,
            }
        } else {
            return {valid: true};
        }
    }

    checkImportance(importance) {
        if (Number.isNaN(importance)) {
            return {
                valid: false,
                message: 'Importance cannot be empty'
            };
        } else {
            return {valid: true};
        }
    }

    renderTextReplacers() {
        let textReplacers = [];

        for (let textReplacer of this.state['text-replacers']) {
            let replacerName = textReplacer.name;
            let body = `${replacerName} (ex. ${textReplacer.example})`;

            let element;
            if (replacerName === this.getTextReplacer()) {
                element = <option name={replacerName} selected={true}>{body}</option>;
            } else {
                element = <option name={replacerName}>{body}</option>;
            }

            textReplacers.push(element);
        }

        return textReplacers;
    }

    render() {
        return (
            <div>
                <h3>Obfuscation Plugin Configuration</h3>
                <p>
                    Base Configuration for Obfuscation Processor plugin for Graylog
                </p>
                <dl className="deflist">
                    <dt>Enabled:</dt>
                    <dd>{this.isObfuscationEnabled() ? 'Yes' : 'No'}</dd>
                    <dt>Text Replacer:</dt>
                    <dd>{this.getTextReplacer()}</dd>
                </dl>
                <IfPermitted permissions="clusterconfigentry:edit">
                    <Button bsStyle="info" bsSize="xs" onClick={this.openModal()}>Configure</Button>
                </IfPermitted>
                <BootstrapModalWrapper ref={ref => this.modal = ref} onHide={this.closeModal()}>
                    <Modal.Header closeButton>
                        <Modal.Title>Obfuscation Plugin Configuration</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {this.state.error && <p className="error">{this.state.error}</p>}
                        {!this.isReady()
                            ?
                            <div style={{'margin-top': '10px'}}><LoadingIndicator/></div>
                            :
                            <fieldset>
                                <Input id="is-obfuscation-enabled"
                                       type="checkbox"
                                       ref="is-obfuscation-enabled"
                                       label="Enable Obfuscation Message Processor"
                                       checked={this.isObfuscationEnabled()}
                                       onChange={this.setObfuscationEnabled()}/>
                                <Input id="text-replacer"
                                       type="select"
                                       ref="text-replacer"
                                       label="Text Replacer"
                                       onChange={this.setTextReplacer()}
                                       help={<span>The pattern which will used for obfuscated data</span>}>
                                    {this.renderTextReplacers()}
                                </Input>
                                <OneFieldObjectsTable tableName="Field Names"
                                                      columnName="Field Name"
                                                      values={this.getFieldNames()}
                                                      avialableValues={this.getAvailableFieldNames()}
                                                      onChange={this.setFieldNames()}
                                                      checkValue={this.checkFieldNameValue()}
                                                      help={
                                                          <span>
                                                              The fields will be obfuscated in the log message.
                                                              The obfuscation will be applied only to text fields
                                                          </span>}/>
                                <OneFieldObjectsTable tableName="Stream Titles"
                                                      columnName="Stream Title"
                                                      values={this.getStreamTitles()}
                                                      avialableValues={this.getAvailableStreamTitles()}
                                                      onChange={this.setStreamTitles()}
                                                      checkValue={this.checkStreamTitleValue()}
                                                      help={
                                                          <span>
                                                              The streams whose messages will be obfuscated
                                                          </span>}/>
                                <RegularExpressionsObjectsTable
                                    tableName="Sensitive Regular Expressions"
                                    elementMapping={{
                                        id: "ID",
                                        name: "Name",
                                        pattern: "Pattern",
                                        importance: "Importance"
                                    }}
                                    values={this.getSensitiveRegularExpressions()}
                                    onChange={this.setSensitiveRegularExpressions()}
                                    checkValue={this.checkRegularExpressionValue()}
                                    help={
                                        <span>
                                            The regular expressions which will used for search of sensitive data
                                        </span>}/>
                                <RegularExpressionsObjectsTable
                                    tableName="White Regular Expressions"
                                    elementMapping={{id: "ID", name: "Name", pattern: "Pattern"}}
                                    values={this.getWhiteRegularExpressions()}
                                    onChange={this.setWhiteRegularExpressions()}
                                    checkValue={this.checkRegularExpressionValue()}
                                    help={<span>The regular expressions which will used for skip white data</span>}/>
                            </fieldset>}
                    </Modal.Body>
                    <Modal.Footer>
                        {this.isReady() && <>
                            <Button type="button" onClick={this.closeModal()}>Cancel</Button>
                            <Button type="button" onClick={this.resetConfig()} bsStyle="warning">Reset</Button>
                            <Button type="button"
                                    onClick={this.saveConfig()}
                                    bsStyle="primary"
                                    disabled={!this.isConfigurationValid()}>Save</Button>
                        </>}
                    </Modal.Footer>
                </BootstrapModalWrapper>
            </div>
        );
    }
}

ObfuscationConfiguration.displayName = 'Obfuscation Configuration';

