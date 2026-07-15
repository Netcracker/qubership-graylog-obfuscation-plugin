export default [
  {
    ignores: ["node_modules/**", ".graylog/**", "target/**", "build/**"],
  },
  {
    files: ["**/*.{js,jsx}"],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: "module",
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
      },
      globals: {
        __dirname: "readonly",
        beforeEach: "readonly",
        describe: "readonly",
        document: "readonly",
        expect: "readonly",
        jest: "readonly",
        module: "writable",
        process: "readonly",
        require: "readonly",
        clearTimeout: "readonly",
        setTimeout: "readonly",
        test: "readonly",
        window: "readonly",
      },
    },
    rules: {
      "no-undef": "warn",
    },
  },
];
