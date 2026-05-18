import '@testing-library/jest-dom';
import React from 'react';
import { beforeEach } from 'vitest';

// Mock funcional de localStorage para tests. jsdom expone `window.localStorage`
// pero en algunos runtimes (issue #219 lo descubrió) los setItem/getItem son
// no-op. Este mock simple lo reemplaza con un Map en memoria, suficiente
// para tests de persistencia de preferencias.
if (typeof window !== 'undefined') {
  const store = new Map<string, string>();
  const localStorageMock: Storage = {
    get length() {
      return store.size;
    },
    clear: () => store.clear(),
    getItem: (key: string) => (store.has(key) ? store.get(key)! : null),
    setItem: (key: string, value: string) => {
      store.set(key, String(value));
    },
    removeItem: (key: string) => {
      store.delete(key);
    },
    key: (index: number) => Array.from(store.keys())[index] ?? null,
  };
  Object.defineProperty(window, 'localStorage', {
    value: localStorageMock,
    writable: true,
    configurable: true,
  });

  // Limpiar storage entre tests para evitar contaminación cruzada
  beforeEach(() => {
    store.clear();
  });
}