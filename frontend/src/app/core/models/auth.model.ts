export interface LoginRequest {
  email: string;
  senha: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  nome: string;
  perfil: string;
}

export interface CadastroRequest {
  nome: string;
  email: string;
  cpf: string;
  telefone: string;
  senha: string;
  perfil: string;
}
