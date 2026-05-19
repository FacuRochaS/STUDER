import { HttpInterceptorFn } from '@angular/common/http';

export const languageInterceptor: HttpInterceptorFn = (req, next) => {
  const lang = localStorage.getItem('preferred_language') || 'es';
  return next(req.clone({ setHeaders: { 'Accept-Language': lang } }));
};

