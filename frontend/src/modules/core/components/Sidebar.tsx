'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState } from 'react';
import { navConfig, NavItem } from '../constants/navConfig';

interface SidebarProps {
  isOpen: boolean;
}

export function Sidebar({ isOpen }: SidebarProps) {
  const pathname = usePathname();
  const [expandedItems, setExpandedItems] = useState<Set<string>>(new Set());

  const isActive = (href: string) => {
    if (href === '/dashboard') {
      return pathname === '/dashboard';
    }
    return pathname.startsWith(href);
  };

  const toggleExpand = (label: string) => {
    setExpandedItems((prev) => {
      const next = new Set(prev);
      if (next.has(label)) {
        next.delete(label);
      } else {
        next.add(label);
      }
      return next;
    });
  };

  const isExpanded = (label: string) => expandedItems.has(label);

  const renderNavItem = (item: NavItem, level: number = 0): React.ReactNode => {
    // Enforce max 2 levels
    if (level >= 2) {
      return null;
    }

    const hasChildren = item.children && item.children.length > 0 && level < 1;
    const expanded = isExpanded(item.label);
    const active = item.href ? isActive(item.href) : false;

    if (hasChildren) {
      return (
        <div key={item.label}>
          <button
            onClick={() => toggleExpand(item.label)}
            className={`nav-item w-full ${active ? 'nav-item-active' : ''} ${!isOpen ? 'justify-center' : ''}`}
            title={!isOpen ? item.label : undefined}
          >
            <span className="shrink-0">{item.icon}</span>
            <span className={`whitespace-nowrap overflow-hidden transition-all duration-300 ${isOpen ? 'opacity-100 ml-3 w-auto' : 'opacity-0 w-0 ml-0'}`}>
              {item.label}
            </span>
            {isOpen && (
              <svg
                className={`w-4 h-4 ml-auto transition-transform ${expanded ? 'rotate-90' : ''}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            )}
          </button>
          {isOpen && expanded && (
            <div className="ml-4 mt-1 space-y-1">
              {item.children?.map((child) => renderNavItem(child, level + 1))}
            </div>
          )}
        </div>
      );
    }

    if (item.href) {
      return (
        <Link
          key={item.href}
          href={item.href}
          className={`nav-item ${active ? 'nav-item-active' : ''} ${!isOpen ? 'justify-center' : ''} ${level > 0 ? 'ml-6' : ''}`}
          title={!isOpen ? item.label : undefined}
        >
          <span className="shrink-0">{item.icon}</span>
          <span className={`whitespace-nowrap overflow-hidden transition-all duration-300 ${isOpen ? 'opacity-100 ml-3 w-auto' : 'opacity-0 w-0 ml-0'}`}>
            {item.label}
          </span>
        </Link>
      );
    }

    return null;
  };
  
  return (
    <aside
      className={`bg-white border-r border-gray-200 flex flex-col transition-all duration-300 overflow-hidden shrink-0 ${
        isOpen ? 'w-56' : 'w-20'
      }`}
    >
      <nav className="flex-1 p-3 space-y-1">
        {navConfig.map((item) => renderNavItem(item))}
      </nav>

      <div className="p-3 border-t border-gray-200">
        <p className="text-xs text-gray-400 text-center whitespace-nowrap overflow-hidden">
          {isOpen ? 'NEXUS v0.1.0' : 'v0.1'}
        </p>
      </div>
    </aside>
  );
}
