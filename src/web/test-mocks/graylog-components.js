import React from "react";

export const BootstrapModalWrapper = React.forwardRef(({ children }, ref) => {
  React.useImperativeHandle(ref, () => ({
    close: jest.fn(),
    open: jest.fn(),
  }));

  return <div>{children}</div>;
});

export const Button = ({ children, ...props }) => (
  <button type="button" {...props}>
    {children}
  </button>
);
export const HelpBlock = ({ children }) => <div>{children}</div>;
export const Icon = ({ name, onClick }) => (
  <button aria-label={name} type="button" onClick={onClick} />
);
export const IfPermitted = ({ children }) => <>{children}</>;
export const Input = ({ children, type, checked, onChange }) => {
  if (type === "select") {
    return <select onChange={onChange}>{children}</select>;
  }

  if (type === "checkbox") {
    return <input checked={checked} onChange={onChange} type="checkbox" />;
  }

  return <input onChange={onChange} type={type || "text"} />;
};
export const LoadingIndicator = () => <div>Loading</div>;
export const Modal = {
  Body: ({ children }) => <div>{children}</div>,
  Footer: ({ children }) => <div>{children}</div>,
  Header: ({ children }) => <div>{children}</div>,
  Title: ({ children }) => <div>{children}</div>,
};
