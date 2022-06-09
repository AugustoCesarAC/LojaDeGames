package com.generation.lojadegames.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.lojadegames.model.Usuario;
import com.generation.lojadegames.model.UsuarioLogin;
import com.generation.lojadegames.repository.UsuarioRepository;

/**
 * A Anotação @Service indica que esta é uma Classe de Serviço, ou seja,
 * implementa todas regras de negócio do Recurso Usuário.
 */

@Service
public class UsuarioService
{

	@Autowired
	private UsuarioRepository usuarioRepository;

	// ------------------------------------------------ CADASTRO E ATT USUARIO
	// --------------------------------------------------- \\

	public Optional<Usuario> cadastrarUsuario(Usuario usuario)
	{

		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			return Optional.empty();

		usuario.setSenha(criptografarSenha(usuario.getSenha()));
		return Optional.of(usuarioRepository.save(usuario));
	}

	public Optional<Usuario> atualizarUsuario(Usuario usuario)
	{

		if (usuarioRepository.findById(usuario.getId()).isPresent())
		{
			Optional<Usuario> buscaUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());
			
			if (buscaUsuario.isPresent() && buscaUsuario.get().getId() != usuario.getId())
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Usuário já existe",null);
			
				usuario.setSenha(criptografarSenha(usuario.getSenha()));
				return Optional.ofNullable(usuarioRepository.save(usuario));
			
		}

		return Optional.empty();
	}

	public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin)
	{
		Optional<Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());
		if (usuario.isPresent())
		{
			if (compararSenhas(usuarioLogin.get().getSenha(), usuario.get().getSenha()))
			{
				usuarioLogin.get().setId(usuario.get().getId());
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setFoto(usuario.get().getFoto());
				usuarioLogin.get()
						.setToken(gerarBasicToken(usuarioLogin.get().getUsuario(), usuarioLogin.get().getSenha()));
				usuarioLogin.get().setSenha(usuario.get().getSenha());

				return usuarioLogin;
			}
		}
		return Optional.empty();
	}

	private String gerarBasicToken(String usuario, String senha)
	{
		String token = usuario + ":" + senha;
		byte[] tokenBase64 = Base64.encodeBase64(token.getBytes(Charset.forName("US-ASCII")));
		return "Basic " + new String(tokenBase64);
	}

	private boolean compararSenhas(String senhaDigitada, String senhaBanco)
	{
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.matches(senhaDigitada, senhaBanco);
	}

	private String criptografarSenha(String senha)
	{
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.encode(senha);
	}

}
