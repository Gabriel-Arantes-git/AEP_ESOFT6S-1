import { FormControl, FormGroup } from '@angular/forms';
import { cpfValidator, senhaForteValidator, senhasIguaisValidator } from './form.validators';

describe('cpfValidator', () => {
  const validar = (valor: string | null) => cpfValidator(new FormControl(valor));

  it('deve aceitar CPF válido com e sem máscara', () => {
    expect(validar('529.982.247-25')).toBeNull();
    expect(validar('52998224725')).toBeNull();
  });

  it.each([
    ['vazio', ''],
    ['nulo', null],
    ['curto', '123'],
    ['dígitos repetidos', '11111111111'],
    ['primeiro dígito verificador errado', '52998224735'],
    ['segundo dígito verificador errado', '52998224726'],
  ])('deve rejeitar CPF %s', (_nome, valor) => {
    expect(validar(valor)).toEqual({ cpf: true });
  });
});

describe('senhaForteValidator', () => {
  const validar = (valor: string | null) => senhaForteValidator(new FormControl(valor));

  it('deve aceitar senha forte', () => {
    expect(validar('Abcdef1!')).toBeNull();
  });

  it.each([
    ['curta', 'Ab1!', { minLength: true }],
    ['nula', null, { minLength: true }],
    ['sem maiúscula', 'abcdef1!', { semMaiuscula: true }],
    ['sem minúscula', 'ABCDEF1!', { semMinuscula: true }],
    ['sem número', 'Abcdefg!', { semNumero: true }],
    ['sem especial', 'Abcdefg1', { semEspecial: true }],
  ])('deve rejeitar senha %s', (_nome, valor, erro) => {
    expect(validar(valor)).toEqual(erro);
  });
});

describe('senhasIguaisValidator', () => {
  const grupo = (senha: string, confirmarSenha: string) =>
    new FormGroup({ senha: new FormControl(senha), confirmarSenha: new FormControl(confirmarSenha) });

  it('deve aceitar senhas iguais', () => {
    expect(senhasIguaisValidator(grupo('Abc123!x', 'Abc123!x'))).toBeNull();
  });

  it('deve rejeitar senhas diferentes', () => {
    expect(senhasIguaisValidator(grupo('Abc123!x', 'outra'))).toEqual({ senhasDiferentes: true });
  });

  it('não deve reclamar enquanto a confirmação está vazia', () => {
    expect(senhasIguaisValidator(grupo('Abc123!x', ''))).toBeNull();
  });
});
