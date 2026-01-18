/**
 * Validates an email address using a safe, non-backtracking regex pattern.
 * This pattern avoids catastrophic backtracking that could lead to ReDoS.
 * 
 * Pattern explanation:
 * - ^[a-zA-Z0-9._%+-]+ : One or more alphanumeric characters, dots, underscores, percent, plus, or hyphens
 * - @ : Required @ symbol
 * - [a-zA-Z0-9.-]+ : One or more alphanumeric characters, dots, or hyphens (domain name)
 * - \.[a-zA-Z]{2,}$ : A dot followed by 2 or more letters (TLD)
 * 
 * This pattern avoids nested quantifiers and catastrophic backtracking.
 * 
 * @param {string} value - The email address to validate
 * @returns {boolean} - True if the email is valid, false otherwise
 */
export const isValidEmail = (value) => {
  // Early return for empty or too long values
  if (!value || typeof value !== 'string') return false;
  if (value.length > 254) return false;
  
  // Use a safe, non-backtracking regex pattern
  // This pattern avoids nested quantifiers that can cause ReDoS
  const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  
  return emailPattern.test(value);
};
