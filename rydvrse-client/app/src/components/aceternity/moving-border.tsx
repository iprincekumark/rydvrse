"use client";
import React from "react";
import { motion } from "framer-motion";
import { cn } from "@/lib/utils";

export const MovingBorder = ({
  children,
  duration = 2000,
  className,
  containerClassName,
  borderClassName,
  as: Component = "button",
  ...otherProps
}: {
  children: React.ReactNode;
  duration?: number;
  className?: string;
  containerClassName?: string;
  borderClassName?: string;
  as?: React.ElementType;
  [key: string]: unknown;
}) => {
  return (
    <Component
      className={cn(
        "relative h-12 w-40 overflow-hidden bg-transparent p-[1px] text-xl",
        containerClassName
      )}
      {...otherProps}
    >
      <div
        className="absolute inset-0"
        style={{ borderRadius: "inherit" }}
      >
        <motion.div
          initial={{ rotate: 0 }}
          animate={{ rotate: 360 }}
          transition={{
            duration: duration / 1000,
            repeat: Infinity,
            ease: "linear",
          }}
          style={{
            position: "absolute",
            inset: "-100%",
            background: `conic-gradient(from 0deg, transparent 0 340deg, hsl(217 91% 60%) 360deg)`,
          }}
          className={cn(borderClassName)}
        />
      </div>

      <div
        className={cn(
          "relative flex h-full w-full items-center justify-center rounded-[inherit] bg-background text-sm antialiased backdrop-blur-xl",
          className
        )}
      >
        {children}
      </div>
    </Component>
  );
};
