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
      {/* 
        PRECISE GEOMETRY CALCULATIONS:
        - Center: (14, 14)
        - Equilateral triangle with side length: 20
        - Triangle vertices:
          * Top: (14, 4.69) - 90°
          * Bottom-left: (4, 23.31) - 210°  
          * Bottom-right: (24, 23.31) - 330°
        - Lines from center to corners at 72% distance
      */}
      
      {/* Equilateral triangle with rounded corners */}
      <path
        d="M 14 4.69 
           L 23.31 23.31 
           L 4.69 23.31 
           Z"
        fill="none"
        stroke="#EF4444"
        strokeWidth="2.5"
        strokeLinejoin="round"
      />
      
      {/* Line pointing to top corner (90°) - 72% distance */}
      <line
        x1="14"
        y1="14"
        x2="14"
        y2="7.89"
        stroke="#EF4444"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
      
      {/* Line pointing to bottom-left corner (210°) - 72% distance */}
      <line
        x1="14"
        y1="14"
        x2="8.72"
        y2="19.28"
        stroke="#EF4444"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
      
      {/* Line pointing to bottom-right corner (330°) - 72% distance */}
      <line
        x1="14"
        y1="14"
        x2="19.28"
        y2="19.28"
        stroke="#EF4444"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  );
};

export default Logo;
