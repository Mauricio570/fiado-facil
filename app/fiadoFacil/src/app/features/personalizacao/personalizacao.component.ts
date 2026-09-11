import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { TamanhoTexto } from '../../core/models/preferencia.model';
import { PreferenciaService } from '../../core/services/preferencia.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { MascaraDirective } from '../../shared/directives/mascara.directive';
import { mensagemDoErro } from '../../shared/utils/erro.util';

interface OpcaoTamanho {
  valor: TamanhoTexto;
  titulo: string;
  descricao: string;
  amostra: string;
}

@Component({
  selector: 'app-personalizacao',
  imports: [MascaraDirective, ReactiveFormsModule],
  templateUrl: './personalizacao.component.html',
  styleUrl: './personalizacao.component.css',
})
export class PersonalizacaoComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  protected readonly preferenciaService = inject(PreferenciaService);

  protected readonly opcoesTamanho: OpcaoTamanho[] = [
    { valor: 'PEQUENO', titulo: 'Pequeno', descricao: 'Mais conteúdo na tela', amostra: 'Aa' },
    { valor: 'PADRAO', titulo: 'Padrão', descricao: 'Tamanho recomendado', amostra: 'Aa' },
    { valor: 'GRANDE', titulo: 'Grande', descricao: 'Leitura mais confortável', amostra: 'Aa' },
  ];

  protected readonly carregando = signal(true);
  protected readonly salvandoTamanho = signal(false);
  protected readonly salvandoDados = signal(false);
  protected readonly mensagemErroTamanho = signal('');
  protected readonly mensagemErroDados = signal('');
  protected readonly mensagemSucesso = signal('');

  protected readonly formulario = this.formBuilder.nonNullable.group({
    nomeEmpresa: ['', [Validators.required, Validators.maxLength(150)]],
    cnpj: ['', [Validators.required, Validators.maxLength(18)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    senhaAtual: [''],
    novaSenha: ['', [Validators.minLength(8), Validators.maxLength(100)]],
    repetirNovaSenha: [''],
  });

  ngOnInit(): void {
    this.preferenciaService.carregar().subscribe({
      error: () => this.mensagemErroTamanho.set('Não foi possível carregar suas preferências.'),
    });

    this.usuarioService
      .buscarLogado()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (usuario) =>
          this.formulario.patchValue({
            nomeEmpresa: usuario.nomeEmpresa,
            cnpj: usuario.cnpj,
            email: usuario.email,
          }),
        error: (erro) =>
          this.mensagemErroDados.set(mensagemDoErro(erro, 'Não foi possível carregar seus dados.')),
      });
  }

  protected escolherTamanho(tamanho: TamanhoTexto): void {
    if (this.salvandoTamanho() || this.preferenciaService.tamanhoTexto() === tamanho) {
      return;
    }

    const anterior = this.preferenciaService.tamanhoTexto();

    this.mensagemErroTamanho.set('');
    this.salvandoTamanho.set(true);

    this.preferenciaService
      .salvar(tamanho)
      .pipe(finalize(() => this.salvandoTamanho.set(false)))
      .subscribe({
        error: (erro) => {
          // Não deixa a tela em um tamanho que o banco não guardou.
          this.preferenciaService.salvar(anterior).subscribe({ error: () => undefined });
          this.mensagemErroTamanho.set(
            mensagemDoErro(erro, 'Não foi possível salvar o tamanho do texto.'),
          );
        },
      });
  }

  protected salvarDados(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const trocandoSenha = valores.novaSenha.trim().length > 0;

    if (trocandoSenha && valores.novaSenha !== valores.repetirNovaSenha) {
      this.mensagemErroDados.set('A nova senha e a repetição não coincidem.');
      return;
    }

    this.mensagemErroDados.set('');
    this.mensagemSucesso.set('');
    this.salvandoDados.set(true);

    this.usuarioService
      .atualizarLogado({
        nomeEmpresa: valores.nomeEmpresa.trim(),
        cnpj: valores.cnpj.trim(),
        email: valores.email.trim(),
        senhaAtual: trocandoSenha ? valores.senhaAtual : null,
        novaSenha: trocandoSenha ? valores.novaSenha : null,
        repetirNovaSenha: trocandoSenha ? valores.repetirNovaSenha : null,
      })
      .pipe(finalize(() => this.salvandoDados.set(false)))
      .subscribe({
        next: () => {
          this.formulario.patchValue({ senhaAtual: '', novaSenha: '', repetirNovaSenha: '' });
          this.mensagemSucesso.set(
            trocandoSenha
              ? 'Dados atualizados. Use a nova senha no próximo acesso.'
              : 'Dados atualizados com sucesso.',
          );
        },
        error: (erro) =>
          this.mensagemErroDados.set(mensagemDoErro(erro, 'Não foi possível salvar seus dados.')),
      });
  }

  protected invalido(campo: 'nomeEmpresa' | 'cnpj' | 'email' | 'novaSenha'): boolean {
    const controle = this.formulario.controls[campo];

    return controle.invalid && controle.touched;
  }
}
