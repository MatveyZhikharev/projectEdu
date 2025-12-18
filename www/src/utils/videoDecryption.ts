/**
 * Video decryption utility for AES-CBC encrypted video chunks
 * 
 * The backend encrypts video chunks using AES/CBC/PKCS5Padding algorithm.
 * This utility decrypts the chunks on the client side.
 */

/**
 * Decrypts an AES-CBC encrypted chunk using Web Crypto API
 * 
 * @param encryptedData - Base64 encoded encrypted data
 * @param iv - Base64 encoded initialization vector
 * @param key - Base64 encoded AES key
 * @returns Decrypted data as Uint8Array
 */
export async function decryptChunk(
  encryptedData: string,
  iv: string,
  key: string
): Promise<Uint8Array> {
  // Decode base64 strings to Uint8Array
  const encryptedBytes = base64ToUint8Array(encryptedData)
  const ivBytes = base64ToUint8Array(iv)
  const keyBytes = base64ToUint8Array(key)

  // Import the key for AES-CBC decryption
  const cryptoKey = await crypto.subtle.importKey(
    'raw',
    keyBytes.buffer as ArrayBuffer,
    { name: 'AES-CBC' },
    false,
    ['decrypt']
  )

  // Decrypt the data
  const decryptedBuffer = await crypto.subtle.decrypt(
    { name: 'AES-CBC', iv: ivBytes.buffer as ArrayBuffer },
    cryptoKey,
    encryptedBytes.buffer as ArrayBuffer
  )

  return new Uint8Array(decryptedBuffer)
}

/**
 * Converts a base64 string to Uint8Array
 */
export function base64ToUint8Array(base64: string): Uint8Array {
  const binaryString = atob(base64)
  const bytes = new Uint8Array(binaryString.length)
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i)
  }
  return bytes
}

/**
 * Converts Uint8Array to base64 string
 */
export function uint8ArrayToBase64(bytes: Uint8Array): string {
  let binary = ''
  const len = bytes.byteLength
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i] as number)
  }
  return btoa(binary)
}

/**
 * Creates a Blob URL from decrypted video chunks
 * 
 * @param chunks - Array of decrypted Uint8Array chunks
 * @param mimeType - Video MIME type (e.g., 'video/mp4')
 * @returns Blob URL for the video
 */
export function createVideoBlobUrl(chunks: Uint8Array[], mimeType: string): string {
  // Convert Uint8Array to ArrayBuffer for Blob compatibility
  const blobParts = chunks.map(chunk => chunk.buffer as ArrayBuffer)
  const blob = new Blob(blobParts, { type: mimeType })
  return URL.createObjectURL(blob)
}

/**
 * Revokes a Blob URL to free memory
 */
export function revokeVideoBlobUrl(url: string): void {
  URL.revokeObjectURL(url)
}

/**
 * Video streaming manager for chunked encrypted video playback
 */
export class EncryptedVideoPlayer {
  private chunks: (Uint8Array | undefined)[] = []
  private blobUrl: string | null = null
  private mimeType: string = 'video/mp4'
  private totalChunks: number = 0
  private loadedChunks: number = 0
  private decryptionKey: string = ''

  constructor(mimeType: string, totalChunks: number, decryptionKey: string) {
    this.mimeType = mimeType
    this.totalChunks = totalChunks
    this.decryptionKey = decryptionKey
    this.chunks = new Array(totalChunks).fill(undefined)
  }

  /**
   * Add and decrypt a chunk
   */
  async addChunk(chunkIndex: number, encryptedData: string, iv: string): Promise<void> {
    const decrypted = await decryptChunk(encryptedData, iv, this.decryptionKey)
    this.chunks[chunkIndex] = decrypted
    this.loadedChunks++
  }

  /**
   * Check if all chunks are loaded
   */
  isComplete(): boolean {
    return this.loadedChunks === this.totalChunks
  }

  /**
   * Get loading progress (0-100)
   */
  getProgress(): number {
    return Math.round((this.loadedChunks / this.totalChunks) * 100)
  }

  /**
   * Create video URL from all loaded chunks
   */
  createVideoUrl(): string {
    if (this.blobUrl) {
      URL.revokeObjectURL(this.blobUrl)
    }
    
    // Filter out undefined chunks and create blob
    const validChunks = this.chunks.filter((c): c is Uint8Array => c !== undefined)
    this.blobUrl = createVideoBlobUrl(validChunks, this.mimeType)
    return this.blobUrl
  }

  /**
   * Create partial video URL for progressive playback
   */
  createPartialVideoUrl(): string | null {
    // Get consecutive chunks from the beginning
    const consecutiveChunks: Uint8Array[] = []
    for (let i = 0; i < this.totalChunks; i++) {
      const chunk = this.chunks[i]
      if (chunk !== undefined) {
        consecutiveChunks.push(chunk)
      } else {
        break
      }
    }
    
    if (consecutiveChunks.length === 0) {
      return null
    }
    
    return createVideoBlobUrl(consecutiveChunks, this.mimeType)
  }

  /**
   * Clean up resources
   */
  destroy(): void {
    if (this.blobUrl) {
      URL.revokeObjectURL(this.blobUrl)
      this.blobUrl = null
    }
    this.chunks = []
    this.loadedChunks = 0
  }
}
