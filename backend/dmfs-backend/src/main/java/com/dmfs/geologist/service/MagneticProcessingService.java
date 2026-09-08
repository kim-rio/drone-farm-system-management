package com.dmfs.geologist.service;

import com.dmfs.auth.entity.User;
import com.dmfs.geologist.dto.ProcessingResponse;
import com.dmfs.geologist.entity.*;
import com.dmfs.geologist.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class MagneticProcessingService {
    private final SurveyRepository surveys; private final SurveyDataRepository surveyData; private final ProcessedMagneticDataRepository processedData; private final MagneticAnomalyMapRepository maps;
    private final Path storageRoot;
    public MagneticProcessingService(SurveyRepository surveys, SurveyDataRepository surveyData, ProcessedMagneticDataRepository processedData, MagneticAnomalyMapRepository maps, @Value("${app.storage.path:uploads}") String storagePath) {
        this.surveys=surveys; this.surveyData=surveyData; this.processedData=processedData; this.maps=maps; this.storageRoot=Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @Transactional
    public ProcessingResponse uploadAndProcess(Long surveyId, MultipartFile file, User operator) {
        if (file.isEmpty()) throw new IllegalArgumentException("A non-empty CSV file is required");
        if (!Objects.requireNonNullElse(file.getOriginalFilename(), "").toLowerCase(Locale.ROOT).endsWith(".csv")) throw new IllegalArgumentException("Raw magnetic data must be supplied as a CSV file");
        Survey survey = surveys.findById(surveyId).orElseThrow(() -> new IllegalArgumentException("Survey not found: " + surveyId));
        if (!survey.getOperator().getId().equals(operator.getId())) throw new IllegalArgumentException("This survey is not assigned to the authenticated drone operator");
        try {
            Path surveyFolder = storageRoot.resolve("surveys").resolve(String.valueOf(surveyId)); Files.createDirectories(surveyFolder);
            String token = UUID.randomUUID().toString(); Path rawFile = surveyFolder.resolve("raw-" + token + ".csv"); file.transferTo(rawFile);
            List<Sample> samples = parseCsv(rawFile);
            if (samples.size() < 3) throw new IllegalArgumentException("CSV needs at least three numeric measurements with latitude, longitude, and magnetic field columns");
            SurveyData raw = new SurveyData(); raw.setSurvey(survey); raw.setCompanyId(survey.getCompany().getId()); raw.setFileName(file.getOriginalFilename()); raw.setFilePath(rawFile.toString()); raw.setFileType(file.getContentType()); raw.setFileSize(Files.size(rawFile)); raw.setRecordCount((long)samples.size()); raw.setUploadedBy(operator.getId()); raw.setUploadedAt(OffsetDateTime.now()); surveyData.save(raw);
            double mean=samples.stream().mapToDouble(Sample::magnetic).average().orElseThrow(); double deviation=Math.sqrt(samples.stream().mapToDouble(s -> Math.pow(s.magnetic()-mean, 2)).average().orElse(0));
            List<Sample> anomalies=samples.stream().filter(s -> deviation > 0 && Math.abs((s.magnetic()-mean)/deviation) >= 2.0).toList();
            Path processedFile=surveyFolder.resolve("processed-"+token+".csv"); writeProcessedCsv(processedFile, samples, mean, deviation);
            ProcessedMagneticData processed=new ProcessedMagneticData(); processed.setSurvey(survey); processed.setSurveyData(raw); processed.setFileName("processed-"+file.getOriginalFilename()); processed.setFilePath(processedFile.toString()); processed.setFileType("text/csv"); processed.setFileSize(Files.size(processedFile)); processed.setRecordCount((long)samples.size()); processed.setProcessedAt(OffsetDateTime.now()); processedData.save(processed);
            Path mapFile=surveyFolder.resolve("anomaly-map-"+token+".svg"); writeSvgMap(mapFile, samples, anomalies);
            MagneticAnomalyMap map=new MagneticAnomalyMap(); map.setSurvey(survey); map.setProcessedMagneticDataId(processed.getId()); map.setMapName(survey.getSurveyCode()+" anomaly map"); map.setFilePath(mapFile.toString()); map.setFileType("image/svg+xml"); map.setFileSize(Files.size(mapFile)); map.setAnomalyCount(anomalies.size()); map.setGeneratedAt(OffsetDateTime.now()); maps.save(map);
            survey.setStatus("COMPLETED");
            return new ProcessingResponse(raw.getId(), processed.getId(), map.getId(), samples.size(), anomalies.size());
        } catch (IOException e) { throw new IllegalStateException("Could not store or process the magnetic data", e); }
    }
    private List<Sample> parseCsv(Path file) throws IOException {
        List<Sample> results=new ArrayList<>(); try (BufferedReader reader=Files.newBufferedReader(file, StandardCharsets.UTF_8)) { String header=reader.readLine(); if(header==null) return results; String[] names=header.toLowerCase(Locale.ROOT).replace(" ","").split(","); int lat=indexOf(names,"latitude","lat"), lon=indexOf(names,"longitude","lon","lng"), mag=indexOf(names,"magnetic","magneticfield","field","mag"); if(lat<0||lon<0||mag<0) throw new IllegalArgumentException("CSV header must include latitude, longitude, and magnetic columns"); String line; while((line=reader.readLine())!=null){String[] row=line.split(",",-1); if(row.length>Math.max(lat,Math.max(lon,mag))) try { results.add(new Sample(Double.parseDouble(row[lat].trim()),Double.parseDouble(row[lon].trim()),Double.parseDouble(row[mag].trim()))); } catch(NumberFormatException ignored) { } } } return results;
    }
    private int indexOf(String[] values, String... options){ for(int i=0;i<values.length;i++) for(String option:options) if(values[i].equals(option)) return i; return -1; }
    private void writeProcessedCsv(Path path,List<Sample> samples,double mean,double sd)throws IOException { try(BufferedWriter out=Files.newBufferedWriter(path)){out.write("latitude,longitude,magnetic,z_score,is_anomaly\n"); for(Sample s:samples){double z=sd==0?0:(s.magnetic-mean)/sd;out.write("%f,%f,%f,%f,%s%n".formatted(s.latitude,s.longitude,s.magnetic,z,Math.abs(z)>=2));}} }
    private void writeSvgMap(Path path,List<Sample> samples,List<Sample> anomalies)throws IOException { double minLat=samples.stream().mapToDouble(Sample::latitude).min().orElse(0),maxLat=samples.stream().mapToDouble(Sample::latitude).max().orElse(1),minLon=samples.stream().mapToDouble(Sample::longitude).min().orElse(0),maxLon=samples.stream().mapToDouble(Sample::longitude).max().orElse(1); StringBuilder svg=new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"900\" height=\"600\" viewBox=\"0 0 900 600\"><rect width=\"900\" height=\"600\" fill=\"#102b35\"/><text x=\"28\" y=\"42\" fill=\"white\" font-family=\"sans-serif\" font-size=\"22\">Magnetic Anomaly Map</text>"); for(Sample s:samples){double x=50+800*(s.longitude-minLon)/Math.max(maxLon-minLon,.000001), y=550-460*(s.latitude-minLat)/Math.max(maxLat-minLat,.000001); boolean anomaly=anomalies.contains(s); svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y).append("\" r=\"").append(anomaly?7:3).append("\" fill=\"").append(anomaly?"#ef6c3c":"#5dc5b8").append("\"/>");} svg.append("<text x=\"28\" y=\"580\" fill=\"#ef6c3c\" font-family=\"sans-serif\">Red: anomaly (|z-score| ≥ 2)</text></svg>"); Files.writeString(path,svg,StandardCharsets.UTF_8); }
    private record Sample(double latitude,double longitude,double magnetic) {}
}
