package com.aep.backend.domain.enums;

public enum StatusSolicitacao {
    ABERTO, TRIAGEM, EM_EXECUCAO, RESOLVIDO, ENCERRADO;

    public boolean podeMoverPara(StatusSolicitacao proximo) {
        return switch (this) {
            case ABERTO      -> proximo == TRIAGEM;
            case TRIAGEM     -> proximo == EM_EXECUCAO || proximo == ENCERRADO;
            case EM_EXECUCAO -> proximo == RESOLVIDO;
            case RESOLVIDO   -> proximo == ENCERRADO;
            case ENCERRADO   -> false;
        };
    }
}
