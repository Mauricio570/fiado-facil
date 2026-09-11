import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';

import { ClienteService } from '../../../core/services/cliente.service';
import { MascaraDirective } from '../../../shared/directives/mascara.directive';
import { montarEndereco, separarEndereco } from '../../../shared/utils/endereco.util';
import { mensagemDoErro } from '../../../shared/utils/erro.util';

@Component({
  selector: 'app-cliente-formulario',
  imports: [MascaraDirective, ReactiveFormsModule],
  templateUrl: './cliente-formulario.component.html',
  styleUrl: './cliente-formulario.component.css',
})
export class ClienteFormularioComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly rotaAtiva = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly clienteId = signal<number | null>(null);
  protected readonly carregando = signal(false);
  protected readonly salvando = signal(false);
  protected readonly mensagemErro = signal('');

  protected readonly formulario = this.formBuilder.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    telefone: ['', [Validators.required, Validators.maxLength(20)]],
    cpf: ['', [Validators.required, Validators.maxLength(14)]],
    cep: [''],
    logradouro: [''],
    numero: [''],
    complemento: [''],
    bairro: [''],
    cidade: [''],
    estado: [''],
  });

  ngOnInit(): void {
    const id = Number(this.rotaAtiva.snapshot.paramMap.get('id'));

    if (!id) {
      return;
    }

    this.clienteId.set(id);
    this.carregando.set(true);

    this.clienteService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (cliente) => {
          this.formulario.patchValue({
            nome: cliente.nome,
            telefone: cliente.telefone ?? '',
            cpf: cliente.cpf ?? '',
            ...separarEndereco(cliente.endereco),
          });
        },
        error: () => this.mensagemErro.set('Não foi possível carregar os dados do cliente.'),
      });
  }

  protected salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const endereco = montarEndereco(valores);

    if (endereco !== null && endereco.length > 255) {
      this.mensagemErro.set('O endereço informado é muito longo. Use no máximo 255 caracteres.');
      return;
    }

    const requisicao = {
      nome: valores.nome.trim(),
      telefone: valores.telefone.trim() || null,
      cpf: valores.cpf.trim() || null,
      endereco,
    };

    const id = this.clienteId();

    this.mensagemErro.set('');
    this.salvando.set(true);

    const operacao = id
      ? this.clienteService.editar(id, requisicao)
      : this.clienteService.cadastrar(requisicao);

    operacao.pipe(finalize(() => this.salvando.set(false))).subscribe({
      next: (cliente) => void this.router.navigate(['/clientes', cliente.id]),
      error: (erro) =>
        this.mensagemErro.set(
          mensagemDoErro(erro, 'Não foi possível salvar o cliente. Confira os dados informados.'),
        ),
    });
  }

  protected cancelar(): void {
    const id = this.clienteId();

    void this.router.navigate(id ? ['/clientes', id] : ['/clientes']);
  }

  protected invalido(campo: 'nome' | 'telefone' | 'cpf'): boolean {
    const controle = this.formulario.controls[campo];

    return controle.invalid && controle.touched;
  }
}
