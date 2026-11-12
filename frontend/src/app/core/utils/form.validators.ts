import { AbstractControl, ValidationErrors } from '@angular/forms';

export function cpfValidator(control: AbstractControl): ValidationErrors | null {
  const digits = (control.value ?? '').replace(/\D/g, '');
  if (digits.length !== 11) return { cpf: true };
  if (/^(.)\1+$/.test(digits)) return { cpf: true };

  const calc = (len: number): number => {
    let sum = 0;
    for (let i = 0; i < len; i++) sum += +digits[i] * (len + 1 - i);
    const rest = (sum * 10) % 11;
    return rest >= 10 ? 0 : rest;
  };

  if (calc(9) !== +digits[9] || calc(10) !== +digits[10]) return { cpf: true };
  return null;
}

export function senhaForteValidator(control: AbstractControl): ValidationErrors | null {
  const v: string = control.value ?? '';
  if (v.length < 8) return { minLength: true };
  if (!/[A-Z]/.test(v)) return { semMaiuscula: true };
  if (!/[a-z]/.test(v)) return { semMinuscula: true };
  if (!/\d/.test(v)) return { semNumero: true };
  if (!/[@$!%*?&._\-#^]/.test(v)) return { semEspecial: true };
  return null;
}

export function senhasIguaisValidator(group: AbstractControl): ValidationErrors | null {
  const senha = group.get('senha')?.value;
  const confirmar = group.get('confirmarSenha')?.value;
  if (confirmar && senha !== confirmar) return { senhasDiferentes: true };
  return null;
}
