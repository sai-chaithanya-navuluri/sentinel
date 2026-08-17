package dev.sentinel.embedding;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.embedding.EmbeddingException;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Produces sentence embeddings using a local, pre-trained ONNX model —
 * entirely offline, no API calls, no per-request cost. This is the direct
 * Java-world equivalent of the Python platform's local sentence-transformers
 * usage (see content-core ADR-0011 for the parallel reasoning).
 */
@Service
public class EmbeddingService {

    private ZooModel<String, float[]> model;
    private Predictor<String, float[]> predictor;

    @PostConstruct
    void loadModel() throws ModelNotFoundException, MalformedModelException, IOException {
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls("djl://ai.djl.huggingface.onnxruntime/sentence-transformers/all-MiniLM-L6-v2")
                .optEngine("OnnxRuntime")
                .optTranslator(new MeanPoolingTranslator())
                .build();

        model = criteria.loadModel();
        predictor = model.newPredictor();
    }

    @PreDestroy
    void closeModel() {
        if (predictor != null) predictor.close();
        if (model != null) model.close();
    }

    /**
     * Embeds a piece of text into a fixed-size vector representing its meaning.
     * Texts with similar meaning produce vectors that are close together.
     */
    public float[] embed(String text) throws EmbeddingException {
        try {
            return predictor.predict(text);
        } catch (TranslateException e) {
            throw new EmbeddingException("Failed to embed text", e);
        }
    }
}