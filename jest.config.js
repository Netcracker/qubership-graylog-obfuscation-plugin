module.exports = {
    testEnvironment: 'jsdom',
    roots: ['<rootDir>/src/web'],
    moduleDirectories: ['node_modules', '<rootDir>/src/web'],
    moduleNameMapper: {
        '\\.(css|less|scss)$': 'identity-obj-proxy',
        '^components/bootstrap$': '<rootDir>/src/web/test-mocks/graylog-components.js',
        '^components/common/IconButton$': '<rootDir>/src/web/test-mocks/IconButton.js',
        '^components/common/IfPermitted$': '<rootDir>/src/web/test-mocks/IfPermitted.js',
        '^components/common/LoadingIndicator$': '<rootDir>/src/web/test-mocks/LoadingIndicator.js',
        '^logic/rest/FetchProvider$': '<rootDir>/src/web/test-mocks/FetchProvider.js',
        '^react-bootstrap$': '<rootDir>/src/web/test-mocks/graylog-components.js',
    },
    transform: {
        '^.+\\.[jt]sx?$': ['babel-jest', {
            presets: [
                ['@babel/preset-env', {targets: {node: 'current'}}],
                ['@babel/preset-react', {runtime: 'automatic'}],
            ],
        }],
    },
};
