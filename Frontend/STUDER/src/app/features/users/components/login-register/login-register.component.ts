import { Component, OnInit, OnDestroy } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
  ValidatorFn,
  ReactiveFormsModule
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AuthApiService } from '../../../../core/auth/auth-api.service';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { StepsModule } from 'primeng/steps';

@Component({
  selector: 'app-login-register',
  templateUrl: './login-register.component.html',
  styleUrls: ['./login-register.component.css'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslatePipe,
    StepsModule
  ]
})
export class LoginRegisterComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  loginForm: FormGroup;
  registerForm: FormGroup;

  isRegisterMode = false;
  loading = false;
  error: string | null = null;
  showPassword = false;
  showConfirmPassword = false;
  registerStep = 0;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthApiService,
    private readonly authState: AuthStateService,
    private readonly translate: TranslateService,
    private readonly router: Router
  ) {
    this.loginForm = this.createLoginForm();
    this.registerForm = this.createRegisterForm();
  }

  ngOnInit(): void {
    this.setupLanguageChangeListener();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Crea el formulario reactivo para login
   */
  private createLoginForm(): FormGroup {
    return this.fb.group({
      username: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]],
    });
  }

  /**
   * Crea el formulario reactivo para registro
   */
  private createRegisterForm(): FormGroup {
    return this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      birthDate: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email, Validators.minLength(5), Validators.maxLength(50)]],
      username: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
    }, { validators: this.passwordsMatchValidator() });
  }

  /**
   * Configura el listener para cambios de idioma
   */
  private setupLanguageChangeListener(): void {
    this.translate.onLangChange
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.clearError();
      });
  }

  /**
   * Cambia el modo entre login y registro
   */
  switchMode(): void {
    this.isRegisterMode = !this.isRegisterMode;
    this.clearError();
    this.registerStep = 0;
    this.resetForms();
  }

  /**
   * Avanza al siguiente paso del registro
   */
  nextStep(): void {
    const personalDataGroup = { firstName: this.registerForm.get('firstName'), lastName: this.registerForm.get('lastName'), birthDate: this.registerForm.get('birthDate'), email: this.registerForm.get('email') };

    // Marcar campos como touched para mostrar errores
    Object.values(personalDataGroup).forEach(control => control?.markAsTouched());

    if (this.isPersonalDataStepValid()) {
      this.registerStep = 1;
    }
  }

  /**
   * Retrocede al paso anterior del registro
   */
  prevStep(): void {
    this.registerStep = 0;
  }

  /**
   * Alterna la visibilidad de la contraseña
   */
  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  /**
   * Alterna la visibilidad de la confirmación de contraseña
   */
  toggleShowConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  /**
   * Envía el formulario de login
   */
  submitLogin(): void {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched(this.loginForm);
      return;
    }

    this.loading = true;
    this.clearError();

    this.authState.login(this.loginForm.value)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user) => {
          this.loading = false;
          this.resetForms();
          if (user) {
            this.router.navigate(['/home']);
          } else {
            this.error = this.translate.instant('login.error');
          }
        },
        error: (err: any) => {
          this.loading = false;
          this.error = err?.error?.message || this.translate.instant('login.error');
        }
      });
  }

  /**
   * Envía el formulario de registro
   */
  submitRegister(): void {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched(this.registerForm);
      return;
    }

    this.loading = true;
    this.clearError();

    this.authService.register(this.registerForm.value)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loading = false;
          this.switchMode();
        },
        error: (err: any) => {
          this.loading = false;
          this.error = err?.error?.message || this.translate.instant('register.error');
        }
      });
  }

  /**
   * Obtiene el control del formulario de login
   */
  getLoginControl(controlName: string): AbstractControl | null {
    return this.loginForm.get(controlName);
  }

  /**
   * Obtiene el control del formulario de registro
   */
  getRegisterControl(controlName: string): AbstractControl | null {
    return this.registerForm.get(controlName);
  }

  /**
   * Verifica si el control está inválido y ha sido tocado
   */
  isControlInvalid(control: AbstractControl | null): boolean {
    return !!(control && control.invalid && control.touched);
  }

  /**
   * Verifica si el paso de datos personales es válido
   */
  private isPersonalDataStepValid(): boolean {
    return !!(
      this.registerForm.get('firstName')?.valid &&
      this.registerForm.get('lastName')?.valid &&
      this.registerForm.get('birthDate')?.valid &&
      this.registerForm.get('email')?.valid
    );
  }

  /**
   * Validador personalizado para verificar que las contraseñas coincidan
   */
  private passwordsMatchValidator(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const password = group.get('password')?.value;
      const confirm = group.get('confirmPassword')?.value;
      return password === confirm ? null : { passwordsMismatch: true };
    };
  }

  /**
   * Marca todos los campos del formulario como tocados
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  /**
   * Reinicia los formularios
   */
  private resetForms(): void {
    this.loginForm.reset();
    this.registerForm.reset();
  }

  /**
   * Limpia el mensaje de error
   */
  private clearError(): void {
    this.error = null;
  }
}
