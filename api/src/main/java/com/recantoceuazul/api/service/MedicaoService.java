package com.recantoceuazul.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import com.recantoceuazul.api.repository.MedicaoRepository;
import com.recantoceuazul.api.repository.ResidenciaRepository;
import com.recantoceuazul.api.repository.AtorRepository;
import com.recantoceuazul.api.model.*;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.recantoceuazul.api.dto.MedicaoRequest;

@Service
public class MedicaoService {

    @Autowired
    private InfluxDbService influxDbService;

    @Autowired
    private MedicaoRepository medicaoRepository;

    @Autowired
    private ResidenciaRepository residenciaRepository; 

    @Autowired
    private AtorRepository atorRepository;
    
    public List<Medicao> listarMedicaoPorResidencia(int residenciaId){
        List<Medicao> listaMedicao = medicaoRepository
                .findByResidenciaIdOrderByDataMedicaoDesc(residenciaId);
        return listaMedicao;
    }

    public Medicao registrarNovaMedicao(MedicaoRequest medicaoDTO) throws Exception{

        Optional<Medicao> ultimaMedicaoOpt = medicaoRepository
                .findTopByResidenciaIdOrderByDataMedicaoDesc(medicaoDTO.getResidenciaId());

        Residencia residencia = residenciaRepository.findById(medicaoDTO.getResidenciaId())
                .orElseThrow(() -> new RuntimeException("Residência não encontrada com id: " + medicaoDTO.getResidenciaId()));

        Ator ator = atorRepository.findById(medicaoDTO.getAtorId())
                .orElseThrow(() -> new RuntimeException("Ator não encontrado com id: " + medicaoDTO.getAtorId()));
        
        if (!(ator.getPapel().equals("ADMIN")) && !(ator.getPapel().equals("FISCA"))){
            throw new RuntimeException("Ator não realiza medição");
        }

        float delta;
        if (ultimaMedicaoOpt.isPresent()) {
            Medicao ultimaMedicao = ultimaMedicaoOpt.get();
            if (ultimaMedicao.getDataMedicao().getMonth().equals(LocalDateTime.now().getMonth()) && ultimaMedicao.getDataMedicao().getYear() == LocalDateTime.now().getYear()){
               throw new RuntimeException("Leitura do mês já foi registrada");
            }
            delta = medicaoDTO.getVolumeAgua() - ultimaMedicao.getVolumeAgua();
        } else {
            // Se não houver medição anterior, o delta é 0
            delta = 0;
        }

        // 4. Criar a nova entidade Medicao com todos os dados
        Medicao novaMedicao = new Medicao();
        novaMedicao.setVolumeAgua(medicaoDTO.getVolumeAgua());
        novaMedicao.setDelta(delta);
        novaMedicao.setDataMedicao(LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))); // Data e hora atuais
        novaMedicao.setResidencia(residencia);
        novaMedicao.setAtor(ator);

        // 5. Salvar no banco de dados
        return medicaoRepository.save(novaMedicao);
    }
    @Scheduled(cron = "0 0 0 1 * ?")
    public void consolidarConsumoMesAnterior(){
       List<FluxTable>tables = influxDbService.medicoesDoMesPassado();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                int residenciaId = Integer.parseInt((String)record.getValueByKey("residenciaId"));
                Float totalLitros = ((Double) record.getValue()).floatValue();
                Float leituraHidrante;
                Optional<Medicao> ultimaMedicaoOpt = medicaoRepository
                .findTopByResidenciaIdOrderByDataMedicaoDesc(residenciaId);
                if (ultimaMedicaoOpt.isPresent()) {
                    Medicao ultimaMedicao = ultimaMedicaoOpt.get();
                    leituraHidrante = ultimaMedicao.getVolumeAgua() + totalLitros;                
                }
                else{
                    leituraHidrante = totalLitros;
                }
                MedicaoRequest consolidado = new MedicaoRequest();
                consolidado.setVolumeAgua(leituraHidrante);
                consolidado.setAtorId(18);
                consolidado.setResidenciaId(residenciaId);

                try {
                    registrarNovaMedicao(consolidado);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}