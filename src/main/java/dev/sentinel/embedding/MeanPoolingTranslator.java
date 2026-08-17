package dev.sentinel.embedding;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.io.IOException;
import java.nio.file.Path;

class MeanPoolingTranslator implements Translator<String, float[]> {

    private HuggingFaceTokenizer tokenizer;

    @Override
    public void prepare(TranslatorContext ctx) throws IOException {
        // Load the tokenizer from the model's own downloaded artifacts,
        // rather than re-resolving the model name independently — the model
        // (via Criteria in EmbeddingService) has already been fetched to a
        // local directory by this point.
        Path modelPath = ctx.getModel().getModelPath();
        tokenizer = HuggingFaceTokenizer.newInstance(modelPath, null);
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String input) {
        NDManager manager = ctx.getNDManager();
        Encoding encoding = tokenizer.encode(input);

        long[] ids = encoding.getIds();
        long[] attentionMask = encoding.getAttentionMask();

        NDArray idArray = manager.create(ids);              // shape [seq_len] — no expandDims
        NDArray maskArray = manager.create(attentionMask);   // shape [seq_len]

        ctx.setAttachment("attentionMask", maskArray);
        return new NDList(idArray, maskArray);
    }

    @Override
    public float[] processOutput(TranslatorContext ctx, NDList list) {
        NDArray embeddings = list.getFirst();   // shape: (seq_len, 384) — no batch dim
        NDArray attentionMask = (NDArray) ctx.getAttachment("attentionMask");  // shape: (seq_len,)

        NDArray mask = attentionMask.toType(ai.djl.ndarray.types.DataType.FLOAT32, false)
                .expandDims(-1);          // (seq_len, 1) — broadcasts against hidden dim

        NDArray masked = embeddings.mul(mask);                 // (seq_len, 384)
        NDArray summed = masked.sum(new int[]{0});              // sum over seq_len → (384,)
        NDArray counts = mask.sum(new int[]{0}).clip(1e-9, Double.MAX_VALUE);  // (1,)
        NDArray pooled = summed.div(counts);                    // (384,) — final sentence vector

        return pooled.toFloatArray();
    }
}