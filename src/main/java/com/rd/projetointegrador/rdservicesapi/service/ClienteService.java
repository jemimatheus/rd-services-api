package com.rd.projetointegrador.rdservicesapi.service;

import com.rd.projetointegrador.rdservicesapi.dto.*;
import com.rd.projetointegrador.rdservicesapi.entity.CartaoEntity;
import com.rd.projetointegrador.rdservicesapi.entity.ContratoEntity;
import com.rd.projetointegrador.rdservicesapi.entity.LoginUsuarioEntity;
import com.rd.projetointegrador.rdservicesapi.entity.UsuarioEntity;
import com.rd.projetointegrador.rdservicesapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;

@Service
public class ClienteService {
    //GRUPO1

    //repositories
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private GeneroRepository generoRepository;
    @Autowired private TipoUsuarioRepository tipoUsuarioRepository;
    @Autowired private LoginUsuarioRepository loginUsuarioRepository;
    @Autowired private ContratoRepository contratoRepository;
    @Autowired private CartaoRepository cartaoRepository;

    //services
    //@Autowired private UfService ufService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private GeneroService generoService;
    @Autowired private PlanosService planosService;
    @Autowired private ContratoService contratoService;
    @Autowired private LoginUsuarioService luService;
    @Autowired private CartaoService cartaoService;
    @Autowired private LembreteService lembreteService;
    //@Autowired private EnderecoService enderecoService;

    //TODO: CADASTRO DE USUÁRIO - TELA DE CADASTRO
    //TODO: faltando contato e endereço usuário
    @Transactional
    public String cadastrarCliente(InputCliente inputUsuario){
        UsuarioEntity usuarioEntity = new UsuarioEntity();
        LoginUsuarioEntity loginUsuarioEntity = new LoginUsuarioEntity();
        ContratoEntity contratoEntity = new ContratoEntity();
        CartaoEntity cartaoEntity= new CartaoEntity();

        //VALIDAR CPF
        String cpf = usuarioEntity.getNrCpf();
        List<UsuarioEntity> usuarioExistente = usuarioRepository.findByNrCpf(cpf);

        //VALIDAR E-MAIL
        String email = loginUsuarioEntity.getDsEmail();
        LoginUsuarioEntity loginExistente = loginUsuarioRepository.findByDsEmail(email);


        if(usuarioExistente.isEmpty() && loginExistente == null) {

            //Passando dados do Usuário
            usuarioEntity = usuarioService.conversaoUsuarioEntity(inputUsuario.getUsuario(), usuarioEntity);
            usuarioEntity = usuarioRepository.save(usuarioEntity);
            BigInteger novoId = usuarioEntity.getIdUsuario();

            //Entidade LoginUsuario
            inputUsuario.getLoginUsuario().setIdUsuario(novoId);
            loginUsuarioEntity = luService.conversaoLoginUsuarioEntity(inputUsuario.getLoginUsuario(), loginUsuarioEntity);
            loginUsuarioRepository.save(loginUsuarioEntity);

            //Entidade Contrato
            inputUsuario.getContrato().setIdUsuario(novoId);
            contratoEntity = contratoService.conversaoContratoEntity(inputUsuario.getContrato(), contratoEntity);
            contratoRepository.save(contratoEntity);

            //Entidade Cartao
            inputUsuario.getCartao().setIdUsuario(novoId);
            cartaoEntity = cartaoService.conversaoCartaoEntity(inputUsuario.getCartao(), cartaoEntity);
            cartaoRepository.save(cartaoEntity);

            //Entidade Contato
            //TODO: fazer relacao com contatoService para cadastrar telefone

        /*
        //contato
        private String ddd;
        private String celular;*/

            return "Usuário cadastrado com sucesso";
        }

        return "Erro. Usuário já cadastrado.";
    }

    //TELA DE CADASTRO - GET
    //TODO: faltando recuperar lista de Ufs para o Form
    public FormularioCadastro getFormularioCadastro() {
        FormularioCadastro formularioCadastro = new FormularioCadastro();

        //formularioCadastro.setUfs(ufService.getUfsDTO());
        formularioCadastro.setGenero(generoService.getGenerosDTO());
        formularioCadastro.setPlanos(planosService.getPlanosDTO());
        return formularioCadastro;
    }

    //TELA MEUS DADOS - GET
    //TODO: faltando dados relativos a contato, uf
    public FormularioMeusDados getFormularioMeusDados(BigInteger id) {
        FormularioCadastro formularioCadastro = getFormularioCadastro();
        FormularioMeusDados formularioMeusDados = new FormularioMeusDados();

        Boolean teste = usuarioRepository.existsById(id);
        if(teste) {
            //buscar entities
            UsuarioEntity usuarioEntity = usuarioService.getUsuario(id);
            Usuario usuario = new Usuario();
            usuario = usuarioService.conversaoUsuarioDTO(usuarioEntity, usuario);

            //buscar e-mail de login
            String email = loginUsuarioRepository.findOneByIdUsuario(id).getDsEmail();

            //buscar idPlano no contrato
            List<ContratoEntity> contratosEntities = contratoRepository.findByUsuarioOrderByDtVigencia(usuarioEntity);
            BigInteger idPlanoVigente = contratosEntities.get(0).getPlanosEntity().getIdPlano();

            //buscar lista de contatos
            //Optional<ContatoEntity> optional = repository.findByIdUsuario(id);

            formularioMeusDados.setUsuario(usuario);
            formularioMeusDados.setDsEmail(email);
            //formularioMeusDados.setContato();
            formularioMeusDados.setIdPlano(idPlanoVigente);

        }

        //formularioMeusDados.setUfs(formularioCadastro.getUfs());
        formularioMeusDados.setGenero(formularioCadastro.getGenero());
        formularioMeusDados.setPlanos(formularioCadastro.getPlanos());

        return formularioMeusDados;
    }

    public AreaDoCliente getAreaDoCliente(BigInteger idUsuario){
        AreaDoCliente areaDoCliente = new AreaDoCliente();
        UsuarioEntity usuarioEntity = usuarioRepository.findById(idUsuario).get();

        areaDoCliente.setIdPaciente(usuarioEntity.getIdUsuario());
        areaDoCliente.setNmNome( usuarioEntity.getNmNome());

        List<Lembrete> lembretes = lembreteService.getLembretesIdPaciente(idUsuario);
        areaDoCliente.setLembrete(lembretes);

        return areaDoCliente;
    }

}