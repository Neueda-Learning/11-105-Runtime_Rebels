const MILESTONE_IMAGES = [
  { keywords: ['ferrari', 'lamborghini', 'porsche', 'car', 'vehicle', 'auto', 'supercar', 'sports car', 'bmw', 'mercedes', 'audi', 'mclaren', 'bugatti'], url: 'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=600&q=80' },
  { keywords: ['house', 'home', 'apartment', 'flat', 'property', 'real estate', 'condo', 'villa', 'mansion', 'bungalow', 'down payment'], url: 'https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=600&q=80' },
  { keywords: ['travel', 'vacation', 'trip', 'holiday', 'world tour', 'flight', 'cruise', 'adventure', 'explore', 'backpack'], url: 'https://images.unsplash.com/photo-1488085061387-422e29b40080?w=600&q=80' },
  { keywords: ['retire', 'retirement', 'freedom', 'financial freedom', 'fire', 'independent', 'independence', 'passive income'], url: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&q=80' },
  { keywords: ['education', 'college', 'university', 'degree', 'school', 'mba', 'phd', 'study', 'tuition', 'scholarship'], url: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=600&q=80' },
  { keywords: ['wedding', 'marriage', 'ring', 'bride', 'honeymoon', 'anniversary'], url: 'https://images.unsplash.com/photo-1519741497674-611481863552?w=600&q=80' },
  { keywords: ['business', 'startup', 'company', 'office', 'entrepreneur', 'invest', 'fund', 'venture'], url: 'https://images.unsplash.com/photo-1497366216548-37526070297c?w=600&q=80' },
  { keywords: ['baby', 'child', 'kid', 'family', 'children', 'college fund', 'education fund'], url: 'https://images.unsplash.com/photo-1476703993599-0035a21b17a9?w=600&q=80' },
  { keywords: ['health', 'medical', 'hospital', 'surgery', 'wellness', 'fitness', 'gym'], url: 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=600&q=80' },
  { keywords: ['yacht', 'boat', 'sail', 'ship', 'ocean', 'sea'], url: 'https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?w=600&q=80' },
  { keywords: ['watch', 'rolex', 'luxury', 'jewel', 'diamond', 'gold'], url: 'https://images.unsplash.com/photo-1523170335258-f5ed11844a49?w=600&q=80' },
]

export const DEFAULT_MILESTONE_IMAGE = 'https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?w=600&q=80'

export function getMilestoneImage(name) {
  if (!name) return DEFAULT_MILESTONE_IMAGE

  const lowerName = name.toLowerCase()
  for (const entry of MILESTONE_IMAGES) {
    if (entry.keywords.some((keyword) => lowerName.includes(keyword))) {
      return entry.url
    }
  }

  return DEFAULT_MILESTONE_IMAGE
}