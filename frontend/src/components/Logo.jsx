import React from "react";

const Logo = ({ className = "", size = 26 }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 28 28"
      fill="none"
      className={className}
      xmlns="http://www.w3.org/2000/svg"
    >
      {/* Triangle with slightly curved corners */}
      <path
        d="M 14 3.5 L 24.5 24.5 L 3.5 24.5 Z"
        fill="none"
        stroke="#EF4444"
        strokeWidth="2.5"
        strokeLinejoin="round"
      />
      
      {/* Three inner lines pointing toward each edge */}
      {/* Line pointing to top edge */}
      <line
        x1="14"
        y1="7"
        x2="14"
        y2="20"
        stroke="#EF4444"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
      
      {/* Line pointing to bottom-left edge */}
      <line
        x1="10"
        y1="18"
        x2="18"
        y2="18"
        stroke="#EF4444"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
      
      {/* Line pointing to bottom-right edge */}
      <line
        x1="11"
        y1="15"
        x2="17"
        y2="15"
        stroke="#EF4444"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  );
};

export default Logo;
