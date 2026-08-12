import requests
import logging

logger = logging.getLogger(__name__)


def test_should_successfully_generate_invoice_when_valid_customer_id_is_provided(valid_customer_id, base_url, header):
    """Test that the API generates an invoice correctly for a valid customer."""
    url = f"{base_url}/invoices"
    payload = {"customerId": str(valid_customer_id)}
    response = requests.post(url, json=payload, headers=header)

    assert response.status_code == 200, f"Expected 200, got {response.status_code}"
    data = response.json()

    required_fields = ["customerId", "invoiceDate", "totalAmount", "items"]
    for field in required_fields:
        assert field in data, f"Missing field: {field}"

    assert isinstance(data["items"], list), "Items should be a list"
    logger.info(f"Test Success: Invoice generated correctly for customer {valid_customer_id}.")


def test_should_return_404_when_generating_invoice_for_non_existent_customer(
        base_url, invalid_customer_id, header, customer_id=None):
    """Verify that generating an invoice for a non-existent customer returns 404."""
    url = f"{base_url}/invoices"
    payload = {"customerId": str(invalid_customer_id)}
    response = requests.post(url, json=payload, headers=header)

    assert response.status_code == 404, f"Expected 404, got {response.status_code}"
    logger.info("Test Success: Correctly handled customer not found during invoice generation.")


def test_should_return_404_when_server_error_occurs_during_invoice_generation(base_url, header, error_id):
    """Verify that the API returns 404 when a simulated server error occurs."""
    url = f"{base_url}/invoices"
    payload = {"customerId": str(error_id)}
    response = requests.post(url, json=payload, headers=header)

    assert response.status_code == 404, f"Expected 404, got {response.status_code}"
    logger.info("Test Success: Correctly handled server error during invoice generation.")


def test_should_handle_invoice_generation_idempotently_when_called_multiple_times(customer_id, base_url, header):
    """Verify that repeated invoice generation requests for the same customer are idempotent."""
    url = f"{base_url}/invoices"
    payload = {"customerId": str(customer_id)}

    res1 = requests.post(url, json=payload, headers=header)
    assert res1.status_code == 200

    res2 = requests.post(url, json=payload, headers=header)
    assert res2.status_code == 200
    logger.info("Test Success: Invoice generation idempotency check passed.")


def test_should_return_409_when_generating_invoice_for_inactive_customer(customer_id, base_url, header):
    """Verify that generating an invoice for an inactive customer returns 409 Conflict."""
    url = f"{base_url}/invoices"
    payload = {"customerId": str(customer_id)}
    response = requests.post(url, json=payload, headers=header)
    assert response.status_code == 409, \
        f"Expected 409 for inactive customer invoice generation, got {response.status_code}"
    logger.info("Test Success: Invoice generation for inactive customer handled correctly (409).")


# ==========================================
# PDF Download endpoint tests
# ==========================================

def test_should_successfully_download_invoice_pdf_when_valid_customer_id_is_provided(
        customer_id, base_url, header):
    """Verify that the PDF endpoint returns a valid binary PDF for an existing active customer."""
    url = f"{base_url}/invoices/pdf"
    params = {"customerId": str(customer_id)}
    response = requests.get(url, params=params, headers=header)

    assert response.status_code == 200, \
        f"Expected 200, got {response.status_code}: {response.text}"
    assert response.headers.get("Content-Type", "").startswith("application/pdf"), \
        f"Expected Content-Type application/pdf, got {response.headers.get('Content-Type')}"

    content_disposition = response.headers.get("Content-Disposition", "")
    assert "attachment" in content_disposition, \
        f"Expected Content-Disposition to contain 'attachment', got: {content_disposition}"
    assert ".pdf" in content_disposition, \
        f"Expected filename to contain .pdf, got: {content_disposition}"

    # Verify the response body starts with PDF magic bytes
    assert response.content[:4] == b"%PDF", \
        "Response body does not start with PDF magic bytes (%PDF)"

    logger.info(
        f"Test Success: Invoice PDF downloaded for customer {customer_id}. "
        f"Size={len(response.content)} bytes, Content-Disposition={content_disposition}."
    )


def test_should_return_404_when_downloading_pdf_for_non_existent_customer(
        base_url, invalid_customer_id, header):
    """Verify that the PDF endpoint returns 404 for an unknown customer ID."""
    url = f"{base_url}/invoices/pdf"
    params = {"customerId": str(invalid_customer_id)}
    response = requests.get(url, params=params, headers=header)

    assert response.status_code == 404, f"Expected 404, got {response.status_code}"
    data = response.json()
    assert data.get("errorCode") == "CUSTOMER_NOT_FOUND", \
        f"Expected errorCode CUSTOMER_NOT_FOUND, got {data.get('errorCode')}"
    logger.info("Test Success: PDF download correctly returned 404 for non-existent customer.")


def test_should_return_400_when_downloading_pdf_without_customer_id(base_url, header):
    """Verify that the PDF endpoint returns 400 when the required customerId param is missing."""
    url = f"{base_url}/invoices/pdf"
    response = requests.get(url, headers=header)

    assert response.status_code == 400, \
        f"Expected 400 for missing customerId, got {response.status_code}"
    logger.info("Test Success: PDF download correctly returned 400 for missing customerId param.")


def test_should_return_400_when_downloading_pdf_with_invalid_customer_id_format(base_url, header):
    """Verify that the PDF endpoint returns 400 when the customerId is not a valid UUID."""
    url = f"{base_url}/invoices/pdf"
    params = {"customerId": "not-a-valid-uuid"}
    response = requests.get(url, params=params, headers=header)

    assert response.status_code == 400, \
        f"Expected 400 for invalid UUID format, got {response.status_code}"
    logger.info("Test Success: PDF download correctly returned 400 for invalid UUID format.")