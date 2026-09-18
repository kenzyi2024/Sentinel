/// <reference types="vitest/config" />
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

// Relative base so the production build works both locally and under a GitHub Pages
// project subpath (https://<user>.github.io/sentinel/).
export default defineConfig({
  base: './',
  plugins: [react()],
  server: { port: 5173 },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
  },
});
