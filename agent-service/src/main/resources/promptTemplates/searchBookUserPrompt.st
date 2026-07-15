You must look up the book using the catalog system.

CRITICAL RULE:
1. Do not use your own knowledge or make up book information.
2. First, call the 'searchBook' tool with the keyword: {keyword}.
3. Once you receive the tool response data, extract the books and format them.

populate AgentSearchResponse as follows:

response:
A friendly sentence describing the search result.
books:
Copy every returned book into the books list.
Do not invent books.
Do not invent ISBNs.

If the tool returns no books, return

response:
"No books found."
books:
[]
