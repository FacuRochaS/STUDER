import { Injectable } from '@angular/core';

export interface TextToken {
  type: 'text' | 'user' | 'tag' | 'course' | 'contest' | 'block';
  value: string;
  originalValue?: string; // para mantener la carcasa original (@User vs @user)
}

@Injectable({
  providedIn: 'root'
})
export class RichTextParserService {
  constructor() {}

  /**
   * Parsea un texto y extrae tokens (texto normal y entidades)
   * @param text Texto a parsear
   * @returns Array de tokens con tipo y valor
   */
  parseText(text: string): TextToken[] {
    if (!text) return [];

    const tokens: TextToken[] = [];
    let lastIndex = 0;

    // Crear un array de todas las coincidencias con su tipo
    const matches: Array<{ type: 'user' | 'tag' | 'course' | 'contest' | 'block'; start: number; end: number; value: string; identifier: string }> = [];

    // Patrones para detectar entidades
    const patternConfigs: Array<{ type: 'user' | 'tag' | 'course' | 'contest' | 'block'; pattern: RegExp }> = [
      { type: 'user', pattern: /@([a-zA-Z0-9_-]+)/g },
      { type: 'tag', pattern: /#([a-zA-Z0-9_-]+)/g },
      { type: 'course', pattern: /&([a-zA-Z0-9_-]+)/g },
      { type: 'contest', pattern: /\$([a-zA-Z0-9_-]+)/g },
      { type: 'block', pattern: /%([a-zA-Z0-9_-]+)/g }
    ];

    // Buscar todas las coincidencias
    for (const { type, pattern } of patternConfigs) {
      let match;
      while ((match = pattern.exec(text)) !== null) {
        matches.push({
          type,
          start: match.index,
          end: match.index + match[0].length,
          value: match[0],
          identifier: match[1]
        });
      }
    }

    // Ordenar por posición
    matches.sort((a, b) => a.start - b.start);

    // Procesar matches y generar tokens
    for (const match of matches) {
      // Agregar texto entre el último index y el inicio de este match
      if (lastIndex < match.start) {
        tokens.push({
          type: 'text',
          value: text.substring(lastIndex, match.start)
        });
      }

      // Agregar el token de la entidad
      tokens.push({
        type: match.type,
        value: match.identifier,
        originalValue: match.value
      });

      lastIndex = match.end;
    }

    // Agregar texto restante
    if (lastIndex < text.length) {
      tokens.push({
        type: 'text',
        value: text.substring(lastIndex)
      });
    }

    return tokens.length > 0 ? tokens : [{ type: 'text', value: text }];
  }

  /**
   * Obtiene el prefijo para una entidad según su tipo
   */
  getEntityPrefix(type: string): string {
    const prefixes: Record<string, string> = {
      'user': '@',
      'tag': '#',
      'course': '&',
      'contest': '$',
      'block': '%'
    };
    return prefixes[type] || '';
  }
}

