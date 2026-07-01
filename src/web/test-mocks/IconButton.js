import React from 'react';

export default function IconButton({name, onClick, title}) {
    return <button aria-label={title || name} type="button" onClick={onClick}/>;
}
