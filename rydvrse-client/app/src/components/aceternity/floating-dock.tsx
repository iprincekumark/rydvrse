"use client";
import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { cn } from "@/lib/utils";
import { useNavigate, useLocation } from "react-router-dom";

type FloatingDockItem = {
  title: string;
  icon: React.ReactNode;
  href: string;
};

export const FloatingDock = ({
  items,
  className,
}: {
  items: FloatingDockItem[];
  className?: string;
}) => {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <div
      className={cn(
        "fixed bottom-4 left-1/2 -translate-x-1/2 z-50 flex items-center gap-1 glass rounded-2xl p-2 shadow-glow-lg",
        className
      )}
    >
      {items.map((item) => (
        <IconContainer
          key={item.title}
          {...item}
          isActive={location.pathname === item.href || location.pathname.startsWith(item.href + "/")}
          onClick={() => navigate(item.href)}
        />
      ))}
    </div>
  );
};

function IconContainer({
  title,
  icon,
  isActive,
  onClick,
}: FloatingDockItem & { isActive: boolean; onClick: () => void }) {
  const [hovered, setHovered] = useState(false);

  return (
    <motion.div
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onClick={onClick}
      className="relative flex items-center justify-center cursor-pointer"
    >
      <AnimatePresence>
        {hovered && (
          <motion.div
            initial={{ opacity: 0, y: 10, x: "-50%" }}
            animate={{ opacity: 1, y: 0, x: "-50%" }}
            exit={{ opacity: 0, y: 2, x: "-50%" }}
            className="absolute left-1/2 -top-8 w-fit whitespace-pre rounded-md glass border-white/10 px-2 py-0.5 text-xs text-foreground"
          >
            {title}
          </motion.div>
        )}
      </AnimatePresence>
      <motion.div
        style={{ width: 44, height: 44 }}
        whileHover={{ scale: 1.15 }}
        whileTap={{ scale: 0.95 }}
        className={cn(
          "rounded-full flex items-center justify-center transition-colors",
          isActive
            ? "bg-primary text-primary-foreground shadow-glow"
            : "text-muted-foreground hover:text-foreground hover:bg-white/10"
        )}
      >
        <div className="h-5 w-5 flex items-center justify-center">{icon}</div>
      </motion.div>
    </motion.div>
  );
}
