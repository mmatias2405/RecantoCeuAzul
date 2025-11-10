// Classes do tipo Controller atuam como intermediárias entre os modelos (que contêm as regras de negócio e lógica da aplicação)
// e as views (interfaces de usuário). Elas recebem as requisições do cliente, interagem com os modelos para processar os dados
// e retornam as respostas apropriadas (geralmente em formato JSON) para o frontend.

package com.recantoceuazul.api.controller;

import com.recantoceuazul.api.dto.ConsumoMensalProjection;

// Importação das classes necessárias para manipular objetos de medição,
// realizar operações de banco de dados e lidar com requisições HTTP.

import com.recantoceuazul.api.dto.MedicaoRequest;
import com.recantoceuazul.api.model.Medicao;
import com.recantoceuazul.api.repository.MedicaoRepository;
import com.recantoceuazul.api.service.MedicaoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// Define esta classe como um controlador REST responsável pelas operações relacionadas à entidade "Medicao".
// A anotação @RestController combina @Controller e @ResponseBody, retornando os dados diretamente no corpo da resposta.
@RestController
@RequestMapping("/api/medicao")
@CrossOrigin(origins = "*")
public class MedicaoController {
    
    // Injeta automaticamente o serviço responsável pelas regras de negócio associadas às medições.
    @Autowired
    private MedicaoService medicaoService;
    
    // Injeta o repositório responsável por realizar operações diretas no banco de dados relacionadas às medições.
    @Autowired
    private MedicaoRepository repo;
    
    // GET -> Retorna a lista completa de medições registradas no banco de dados.
    @GetMapping
    public List<Medicao> listar() {
        return repo.findAll();
    }
    
    // GET -> Retorna uma medição específica com base no ID fornecido na URL.
    // Caso o ID não exista, retorna null.
    @GetMapping("/{id}")
    public Medicao buscarPorId(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }
    
    // GET -> Retorna todas as medições associadas a uma residência específica.
    // O ID da residência é passado como parâmetro na URL.
    @GetMapping("/residencia/{id}")
    public List<Medicao> buscarPorResidencia(@PathVariable Integer id) {
        return medicaoService.listarMedicaoPorResidencia(id);
    }
    
    // PUT -> Atualiza uma medição existente com base no ID informado.
    // Caso a medição exista, seus campos são atualizados com os novos valores enviados na requisição.
    // Se o ID não for encontrado, retorna null.
    @PutMapping("/{id}")
    public Medicao atualizar(@PathVariable Integer id, @RequestBody Medicao atualizado) {
        return repo.findById(id).map(existente -> {
            existente.setVolumeAgua(atualizado.getVolumeAgua());
            existente.setDataMedicao(atualizado.getDataMedicao());
            existente.setResidencia(atualizado.getResidencia());
            existente.setDelta(atualizado.getDelta());
            existente.setAtor(atualizado.getAtor());
            return repo.save(existente);
        }).orElse(null);
    }
    
    // DELETE -> Exclui uma medição específica com base no ID informado.
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        repo.deleteById(id);
    }
    
    
    // POST -> Cria e registra uma nova medição no sistema.
    // Recebe um objeto do tipo MedicaoRequest, que contém os dados necessários para o registro.
    // Caso ocorra algum erro de validação ou processamento, retorna um erro 400 (BAD_REQUEST).
    // Se o registro for bem-sucedido, retorna a nova medição com status 201 (CREATED).
    @PostMapping
    public ResponseEntity<?> criarMedicao(@RequestBody MedicaoRequest medicaoDTO) {
        Medicao novaMedicao = new Medicao();
        try {
            novaMedicao = medicaoService.registrarNovaMedicao(medicaoDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return new ResponseEntity<>(novaMedicao, HttpStatus.CREATED);
    }
    
    //Retorna uma média de consumo do condominio
    @GetMapping("/media")
    public List<ConsumoMensalProjection> getMediaConsumo() {
        return repo.getMediaConsumoCondominio();
    }
    
}