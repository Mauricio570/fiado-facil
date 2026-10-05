import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Guia de uso do sistema, organizado por tarefa ("como faço X") e não por tela.
 * O lojista chega com uma intenção, não com o nome de uma tela.
 *
 * O conteúdo vive no template, versionado junto do código — as telas que ele
 * descreve mudam no mesmo commit que o texto. Não há edição pelo sistema.
 *
 * Os `id` das seções são as âncoras usadas pelo link "Ver no guia" dos botões
 * de ajuda (`<app-ajuda secaoGuia="...">`).
 */
@Component({
  selector: 'app-guia',
  imports: [RouterLink],
  templateUrl: './guia.component.html',
  styleUrl: './guia.component.css',
})
export class GuiaComponent {
  protected readonly secoes = [
    { id: 'primeiros-passos', titulo: 'Primeiros passos' },
    { id: 'clientes', titulo: 'Cadastrar e gerenciar clientes' },
    { id: 'vendas', titulo: 'Registrar uma venda fiada' },
    { id: 'parcelamento', titulo: 'Como funcionam as parcelas e os juros' },
    { id: 'recebimentos', titulo: 'Dar baixa em uma parcela' },
    { id: 'alterar-vendas', titulo: 'Editar ou excluir uma compra' },
    { id: 'valores', titulo: 'Entendendo os valores das telas' },
    { id: 'personalizacao', titulo: 'Personalizar o sistema' },
  ];
}
