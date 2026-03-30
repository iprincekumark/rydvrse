"use client";
import React from "react";
import { motion } from "framer-motion";
import { cn } from "@/lib/utils";

export const GlowingEffect = ({
  children,
  className,
  containerClassName,
  color = "hsl(217 91% 60%)",
}: {
  children: React.ReactNode;
  className?: string;
  containerClassName?: string;
  color?: string;
}) => {
  return (
    <div className={cn("relative group", containerClassName)}>
      <motion.div
        className="absolute -inset-0.5 rounded-xl opacity-0 group-hover:opacity-75 transition-opacity duration-500 blur"
        style={{
          background: `linear-gradient(135deg, ${color}, hsl(263 70% 58%))`,
        }}
        animate={{
          backgroundPosition: ["0% 50%", "100% 50%", "0% 50%"],
        }}
        transition={{
          duration: 5,
          repeat: Infinity,
          repeatType: "reverse",
        }}
      />
      <div className={cn("relative", className)}>
        {children}
      </div>
    </div>
  );
};

export const GlowingBorder = ({
  children,
  className,
  active = false,
}: {
  children: React.ReactNode;
  className?: string;
  active?: boolean;
}) => {
  return (
    <div className={cn("relative", className)}>
      {active && (
        <div
          className="absolute -inset-[1px] rounded-xl opacity-75 blur-sm"
          style={{
            background: "linear-gradient(135deg, hsl(217 91% 60%), hsl(263 70% 58%))",
          }}
        />
      )}
      <div className="relative">
        {children}
      </div>
    </div>
  );
};
