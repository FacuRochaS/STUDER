import {
  HttpEvent,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Observable, map } from 'rxjs';

function toCamelCase(obj: any): any {
  if (Array.isArray(obj)) {
    return obj.map(v => toCamelCase(v));
  } else if (obj && typeof obj === 'object') {
    return Object.keys(obj).reduce((result, key) => {
      const camelKey = key.replace(/_([a-z])/g, g => g[1].toUpperCase());
      result[camelKey] = toCamelCase(obj[key]);
      return result;
    }, {} as any);
  }
  return obj;
}

function toSnakeCase(obj: any): any {
  if (Array.isArray(obj)) {
    return obj.map(v => toSnakeCase(v));
  } else if (obj && typeof obj === 'object') {
    return Object.keys(obj).reduce((result, key) => {
      const snakeKey = key.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);
      result[snakeKey] = toSnakeCase(obj[key]);
      return result;
    }, {} as any);
  }
  return obj;
}

export const caseConverterInterceptor = (
  req: HttpRequest<any>,
  next: import('@angular/common/http').HttpHandlerFn
): Observable<HttpEvent<any>> => {

  const isAssetRequest = req.url.includes('/assets/');

  if (isAssetRequest) {
    return next(req);
  }

  let modifiedReq = req;
  if (req.body && typeof req.body === 'object') {
    modifiedReq = req.clone({
      body: toSnakeCase(req.body)
    });
  }
  return next(modifiedReq).pipe(
    map(event => {
      if (event instanceof HttpResponse && event.body) {
        return event.clone({ body: toCamelCase(event.body) });
      }
      return event;
    })
  );
};
