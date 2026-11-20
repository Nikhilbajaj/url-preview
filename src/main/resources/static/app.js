const form = document.querySelector("#preview-form");
const input = document.querySelector("#url");
const status = document.querySelector("#status");
const card = document.querySelector("#preview");
const image = document.querySelector("#preview-image");
const title = document.querySelector("#preview-title");
const description = document.querySelector("#preview-description");
const link = document.querySelector("#preview-url");
const button = form.querySelector("button");

form.addEventListener("submit", async (event) => {
	event.preventDefault();
	status.textContent = "";
	card.hidden = true;
	button.disabled = true;
	button.textContent = "Loading...";

	try {
		const query = new URLSearchParams({ url: input.value });
		const response = await fetch(`/api/preview?${query}`);
		const result = await response.json();
		if (!response.ok) {
			throw new Error(result.message || "Could not create a preview.");
		}

		title.textContent = result.title || "Untitled page";
		description.textContent = result.description || "No description is available.";
		link.href = result.url;
		link.textContent = result.url;
		image.hidden = !result.imageUrl;
		if (result.imageUrl) {
			image.src = result.imageUrl;
		}
		card.hidden = false;
	}
	catch (error) {
		status.textContent = error instanceof Error ? error.message : "Could not create a preview.";
	}
	finally {
		button.disabled = false;
		button.textContent = "Preview";
	}
});
