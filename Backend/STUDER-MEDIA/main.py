import logging
import sys
import json
from fastapi import FastAPI, UploadFile, File, Path, Request
from fastapi.responses import JSONResponse
import boto3
from PIL import Image
import io
import os
import time
from botocore.exceptions import NoCredentialsError, PartialCredentialsError, EndpointConnectionError

# Configure logging
logging.basicConfig(stream=sys.stdout, level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

app = FastAPI()

# Get MinIO configuration from environment variables
MINIO_ENDPOINT = os.getenv('MINIO_ENDPOINT')
MINIO_ACCESS_KEY = os.getenv('MINIO_ROOT_USER')
MINIO_SECRET_KEY = os.getenv('MINIO_ROOT_PASSWORD')
BUCKET_NAME = os.getenv('BUCKET_NAME')

if not all([MINIO_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, BUCKET_NAME]):
    logging.error("🚨 Missing one or more required environment variables for MinIO configuration")
    raise ValueError("Missing one or more required environment variables for MinIO configuration")

s3 = None

def get_s3_client():
    global s3
    if s3 is None:
        logging.info("Creating S3 client...")
        s3 = boto3.client(
            's3',
            endpoint_url=f'http://{MINIO_ENDPOINT}',
            aws_access_key_id=MINIO_ACCESS_KEY,
            aws_secret_access_key=MINIO_SECRET_KEY
        )
    return s3

def set_public_read_policy(s3_client, bucket_name):
    policy = {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Principal": "*",
                "Action": ["s3:GetObject"],
                "Resource": [f"arn:aws:s3:::{bucket_name}/*"]
            }
        ]
    }
    try:
        s3_client.put_bucket_policy(Bucket=bucket_name, Policy=json.dumps(policy))
        logging.info(f"✅ Public read policy set for bucket '{bucket_name}'.")
    except Exception as e:
        logging.error(f"🚨 Failed to set public policy for bucket '{bucket_name}': {e}")

def create_bucket_if_not_exists():
    s3_client = get_s3_client()
    retries = 5
    while retries > 0:
        try:
            s3_client.head_bucket(Bucket=BUCKET_NAME)
            logging.info(f"✅ Bucket '{BUCKET_NAME}' already exists.")
            set_public_read_policy(s3_client, BUCKET_NAME)
            return
        except s3_client.exceptions.ClientError as e:
            if e.response['Error']['Code'] == '404':
                try:
                    s3_client.create_bucket(Bucket=BUCKET_NAME)
                    logging.info(f"✅ Bucket '{BUCKET_NAME}' created successfully.")
                    set_public_read_policy(s3_client, BUCKET_NAME)
                    return
                except Exception as create_error:
                    logging.error(f"🚨 Error creating bucket: {create_error}")
            else:
                logging.error(f"🚨 Error checking for bucket: {e}")
        except (NoCredentialsError, PartialCredentialsError):
            logging.error("🚨 Credentials not available for MinIO.")
            break
        except EndpointConnectionError:
            logging.warning(f"Could not connect to MinIO at {MINIO_ENDPOINT}. Retrying...")
        except Exception as e:
            logging.error(f"🚨 An unexpected error occurred during bucket creation: {e}")

        retries -= 1
        time.sleep(5)
    logging.error("🚨 Failed to connect to MinIO after several retries.")


@app.on_event("startup")
def on_startup():
    create_bucket_if_not_exists()

@app.post("/upload-image/{upload_type}")
async def upload_image(
    request: Request,
    upload_type: str = Path(..., description="The type of image to upload. 'profile' or 'message'"),
    file: UploadFile = File(...)
):
    logging.info(f"➡️  Received request to upload image of type '{upload_type}' from {request.client.host}")
    try:
        contents = await file.read()
        s3_client = get_s3_client()
        urls = {}
        
        logging.info(f"Processing file '{file.filename}' ({len(contents)} bytes)")

        if upload_type == 'profile':
            # --- Profile Image Processing ---
            s3_client.put_object(Bucket=BUCKET_NAME, Key=f"profile/original/{file.filename}", Body=io.BytesIO(contents))
            urls['original'] = f"http://localhost:9000/{BUCKET_NAME}/profile/original/{file.filename}"
            logging.info("Uploaded original profile image.")

            img = Image.open(io.BytesIO(contents))
            
            if img.mode == 'RGBA':
                img = img.convert('RGB')

            img.thumbnail((1080, 1080))
            buffer_1080 = io.BytesIO()
            img.save(buffer_1080, format='JPEG')
            buffer_1080.seek(0)
            s3_client.put_object(Bucket=BUCKET_NAME, Key=f"profile/avatar/{file.filename}", Body=buffer_1080)
            urls['avatar'] = f"http://localhost:9000/{BUCKET_NAME}/profile/avatar/{file.filename}"
            logging.info("Uploaded avatar (1080p) image.")

            img_for_webp = Image.open(io.BytesIO(contents))
            buffer_webp = io.BytesIO()
            img_for_webp.save(buffer_webp, format='WEBP')
            buffer_webp.seek(0)
            s3_client.put_object(Bucket=BUCKET_NAME, Key=f"profile/webp/{file.filename}", Body=buffer_webp)
            urls['webp'] = f"http://localhost:9000/{BUCKET_NAME}/profile/webp/{file.filename}"
            logging.info("Uploaded WebP image.")

            img.thumbnail((150, 150))
            buffer_thumb = io.BytesIO()
            img.save(buffer_thumb, format='JPEG')
            buffer_thumb.seek(0)
            s3_client.put_object(Bucket=BUCKET_NAME, Key=f"profile/thumbnail/{file.filename}", Body=buffer_thumb)
            urls['thumbnail'] = f"http://localhost:9000/{BUCKET_NAME}/profile/thumbnail/{file.filename}"
            logging.info("Uploaded thumbnail (150x150) image.")

        elif upload_type == 'message':
            # --- Message Image Processing ---
            s3_client.put_object(Bucket=BUCKET_NAME, Key=f"message/original/{file.filename}", Body=io.BytesIO(contents))
            urls['original'] = f"http://localhost:9000/{BUCKET_NAME}/message/original/{file.filename}"
            logging.info("Uploaded original message image.")

            img = Image.open(io.BytesIO(contents))
            if img.mode == 'RGBA':
                img = img.convert('RGB')

            img.thumbnail((500, 500))
            buffer_thumb = io.BytesIO()
            img.save(buffer_thumb, format='JPEG')
            buffer_thumb.seek(0)
            s3_client.put_object(Bucket=BUCKET_NAME, Key=f"message/thumbnail/{file.filename}", Body=buffer_thumb)
            urls['thumbnail'] = f"http://localhost:9000/{BUCKET_NAME}/message/thumbnail/{file.filename}"
            logging.info("Uploaded thumbnail (500x500) for message.")

        else:
            logging.warning(f"Invalid upload type received: {upload_type}")
            return JSONResponse(content={"error": "Invalid upload type"}, status_code=400)

        logging.info(f"✅ Successfully processed and uploaded all image versions for '{file.filename}'")
        return JSONResponse(content={"urls": urls})

    except Exception as e:
        logging.error(f"🔥 An error occurred while processing the image upload: {e}", exc_info=True)
        return JSONResponse(content={"error": "An internal error occurred"}, status_code=500)
