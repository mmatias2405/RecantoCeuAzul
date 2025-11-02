// Classes do tipo Controller atuam como intermediárias entre os modelos (que contêm as regras de negócio e lógica da aplicação)
// e as views (interfaces de usuário). Elas recebem as requisições do cliente, interagem com os modelos para processar os dados
// e retornam as respostas apropriadas (geralmente em formato JSON) para o frontend.


package com.recantoceuazul.api.controller;


// Importação das classes necessárias para manipular objetos de residência
// e realizar operações de banco de dados via o repositório correspondente.
import com.recantoceuazul.api.model.Residencia;
import com.recantoceuazul.api.repository.ResidenciaRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;


// Define esta classe como um controlador REST responsável pelas operações relacionadas à entidade "Residencia".
// A anotação @RestController combina @Controller e @ResponseBody, permitindo que os métodos retornem objetos
// diretamente como resposta HTTP em formato JSON.
// @RequestMapping define o endpoint base da API, e @CrossOrigin permite requisições vindas de origens diferentes (como o frontend).
@RestController
@RequestMapping("/api/residencia")
@CrossOrigin(origins = "*") 
public class ResidenciaController {

    // Injeção do repositório responsável por interagir com o banco de dados da entidade "Residencia".
    private final ResidenciaRepository repo;
    
    // Construtor que injeta o repositório de residências.
    public ResidenciaController(ResidenciaRepository repo) {
        this.repo = repo;
    }

    // GET -> Retorna a lista completa de residências registradas no banco de dados.
    @GetMapping
    public List<Residencia> listar() {
        return repo.findAll();
    }

    
    // GET -> Retorna uma residência específica com base no ID informado na URL.
    // Caso o ID seja nulo, igual a zero ou inexistente, retorna null.
    @GetMapping("/{id}")
    public Residencia getByIDResidencia(@PathVariable Integer id) {
        if (id == null || id == 0)
            return null;

        return repo.findById(id).orElse(null);
    }   

    // PUT -> Atualiza os dados de uma residência existente com base no ID informado.
    // Se o registro for encontrado, atualiza o número da residência e salva as alterações no banco de dados.
    // Caso o ID não exista, lança uma exceção informando que a residência não foi encontrada.
    @PutMapping("/{id}")
    public Residencia atualizar(@PathVariable Integer id, @RequestBody Residencia residenciaAtualizado) {
        return repo.findById(id)
            .map(residenciaExistente -> {
                residenciaExistente.setNumero(residenciaAtualizado.getNumero());
                return repo.save(residenciaExistente);
            })
            .orElseThrow(() -> new RuntimeException("Residencia não encontrado com id " + id));
    }

    // POST -> Cria e salva uma nova residência no banco de dados.
    // Recebe um objeto do tipo "Residencia" no corpo da requisição.
    @PostMapping
    public Residencia salvar(@RequestBody Residencia residencia) {
        return repo.save(residencia);
    }

    // DELETE -> Exclui uma residência existente com base no ID informado.
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }

    
}
