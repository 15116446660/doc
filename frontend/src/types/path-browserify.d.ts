declare module 'path-browserify' {
  export function resolve(...paths: string[]): string
  export function normalize(path: string): string
  export function isAbsolute(path: string): boolean
  export function join(...paths: string[]): string
  export function relative(from: string, to: string): string
  export function dirname(path: string): string
  export function basename(path: string, ext?: string): string
  export function extname(path: string): string
  export const sep: string
  export const delimiter: string
} 