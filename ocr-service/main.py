from fastapi import FastAPI, UploadFile, File, HTTPException
from paddleocr import PaddleOCR
import tempfile
import os

app = FastAPI()

ocr = PaddleOCR(
    lang="korean",
    use_textline_orientation=True
)

@app.post("/ocr/receipt")
async def ocr_receipt(file: UploadFile = File(...)):
    suffix = os.path.splitext(file.filename or "receipt.png")[1] or ".png"

    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            tmp.write(await file.read())
            tmp_path = tmp.name

        result = ocr.predict(tmp_path)

        lines = []

        for page in result:
            texts = page.get("rec_texts", [])
            scores = page.get("rec_scores", [])
            boxes = page.get("rec_polys")
            if boxes is None:
                boxes = page.get("rec_boxes")

            for i, text in enumerate(texts):
                confidence = float(scores[i]) if i < len(scores) else None
                box = boxes[i].tolist() if boxes is not None and i < len(boxes) else None

                lines.append({
                    "text": text,
                    "confidence": confidence,
                    "box": box
                })

        return {"lines": lines}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        if "tmp_path" in locals() and os.path.exists(tmp_path):
            os.remove(tmp_path)