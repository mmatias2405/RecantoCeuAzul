// Classes do tipo Controller atuam como intermediárias entre os modelos (que contêm as regras de negócio e lógica da aplicação)
// e as views (interfaces de usuário). Elas recebem as requisições do cliente, interagem com os modelos para processar os dados
// e retornam as respostas apropriadas (geralmente em formato JSON) para o frontend.



// As linhas de código abaixo servem apenas para setar qual é o pacote, assim como, carregar bibliotecas necessárias para a implementação das funções/métodos implementados na classe. 
package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.model.Ator;
import com.recantoceuazul.api.repository.AtorRepository;
import com.recantoceuazul.api.repository.ResidenciaRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


// As linhas de código abaixo estabelecem os diferentes métodos associados ao modelo "ator", sendo que, a primeira anotação (@RestController), apenas indica que estamos construindo um controller do tipo Rest, assim como, cada um dos demais métodos possui uma função específica.

@RestController
@RequestMapping("/api/ator")
@CrossOrigin(origins = "*") // importante para permitir chamadas do frontend
public class AtorController {
    private final AtorRepository repo;
    private final ResidenciaRepository residencias;

    public AtorController(AtorRepository repo, ResidenciaRepository residencias) {
        this.repo = repo;
        this.residencias = residencias;
    }

// Endpoint de login. Recebe as credenciais de um Ator (email e senha) e valida no banco de dados.
// Se as credenciais forem válidas, retorna o ID do ator; caso contrário, retorna erro 401 (não autorizado).

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Ator request) {
        Optional<Ator> adm = repo.findByEmailAndSenha(request.getEmail(), request.getSenha());
        if (adm.isPresent()) {
            return ResponseEntity.ok(adm.get().getId()); // retorna apenas o ID
        } else {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
    }

// Get -> são métodos que solicitam informações que estão salvas no banco de dados e que devem ser exibidas para o usuário. Por exemplo, o método abaixo serve para exibir ao usuário uma lista de atores salva no banco de dados.  
    @GetMapping
    public List<Ator> listar() {
        return repo.findAll();
    }

// Retorna um ator específico com base no ID fornecido na URL.
// Caso o ID não exista, retorna null.
    @GetMapping("/{id}")
    public Ator getByIDAtor(@PathVariable Integer id) {
        if (id == null || id == 0)
            return null;

        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Ator salvar(@RequestBody Ator ator) {
        return repo.save(ator);
    }

// Put -> são métodos que atualizam ou fazem a substituição de um dado previamente salvo no banco de dados.  
// O método abaixo promove um ator existente para o papel de "FISCAL".
// Caso o ator já esteja associado a alguma residência, a promoção não é realizada.
    @PutMapping("/{id}/tofiscal")
    public Ator promoveFiscal(@PathVariable Integer id) {
        return repo.findById(id)
        .map(atorExistente -> {
            if(!atorExistente.getResidencias().isEmpty()){
                return new Ator();
            }
            atorExistente.setPapel("FISCA");
            return repo.save(atorExistente);
        }).orElse(new Ator());
    }

// Promove um ator existente para o papel de "ADMIN".
// Caso o ator já esteja associado a alguma residência, a promoção não é realizada.
    @PutMapping("/{id}/toadmin")
    public Ator promoveAdmin(@PathVariable Integer id) {
        return repo.findById(id)
        .map(atorExistente -> {
            if(!atorExistente.getResidencias().isEmpty()){
                return new Ator();
            }
            atorExistente.setPapel("ADMIN");
            return repo.save(atorExistente);
        }).orElse(new Ator());
    }

// Associa uma residência a um ator, desde que ele não tenha o papel de ADMIN ou FISCAL.
// Se o ator ainda não tiver papel definido, ele é definido como "MORAR".
// Caso a residência informada não exista, um novo objeto vazio é retornado.
    @PutMapping("/residencia/{id}")
    public Ator adicionaResidencia(@PathVariable Integer id, @RequestBody Ator entity) {
        Ator atorArmazenado = this.getByIDAtor(entity.getId());
        if(atorArmazenado.getPapel() == null){
            atorArmazenado.setPapel("MORAR");
        }
        else{
            if(atorArmazenado.getPapel().equals("ADMIN") || atorArmazenado.getPapel().equals("FISCA"))
            return new Ator();
        }
        
        if (residencias.findById(id).isPresent()){
            atorArmazenado.getResidencias().add(residencias.findById(id).get());
        }
        else
            return new Ator();
        return repo.save(atorArmazenado);
    }

// Delete -> são métodos que apagam informações salvas no banco de dados. Por exemplo, o método abaixo serve para apagar atores salvos no banco de dados, buscando o ator que deve ser deletado por meio de seu ID.  
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
